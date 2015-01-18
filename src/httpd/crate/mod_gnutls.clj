; Copyright (c) Michael Jerger. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software.

(ns httpd.crate.mod-gnutls
  (require 
    [clojure.string :as string]
    [pallet.actions :as actions]
    [pallet.stevedore :as stevedore]
    ))

(defn- certs-file-name
  [domain-name]
  (str "/etc/apache2/ssl.crt/" domain-name ".certs")
  )

(defn- key-file-name
  [domain-name]
  (str "/etc/apache2/ssl.key/" domain-name ".key")
  )

(def gnutls-conf
  ["<IfModule mod_gnutls.c>"
   "  # managed by pallet - do not change manually"
   "  GnuTLSCache dbm /var/cache/apache2/gnutls_cache"
   "</IfModule>"])

(defn gnutls-certs
  [ & {:keys [domain-cert
              intermediate-certs
              ca-cert]
       :or {intermediate-certs []
            ca-cert ""}}]
  (into 
    []
    (concat
      [domain-cert]
      intermediate-certs
      [ca-cert
       ""]
      ))
  )

(defn configure-gnutls-credentials
  [ & {:keys [domain-name
              domain-cert
              domain-key
              intermediate-certs
              ca-cert]
       :or {intermediate-certs []
            ca-cert ""}}]
  (assert domain-name)
  (assert domain-cert)
  (assert domain-key)
  (actions/remote-file
    (certs-file-name domain-name)
    :owner "root"
    :group "root"
    :mode "600"
    :content
    (string/join
      \newline 
      (gnutls-certs 
        :domain-cert domain-cert
        :intermediate-certs intermediate-certs
        :ca-cert ca-cert)))
  (actions/remote-file
    (key-file-name domain-name)
    :owner "root"
    :group "root"
    :mode "600"
    :content domain-key)
  )

(defn vhost-gnutls 
  [domain-name]
  ["GnuTLSEnable on"
   "GnuTLSCacheTimeout 300"
   "GnuTLSPriorities SECURE:!VERS-SSL3.0:!MD5:!DHE-RSA:!DHE-DSS:!AES-256-CBC:%COMPAT"
   "GnuTLSExportCertificates on"
   ""
   (str "GnuTLSCertificateFile " (certs-file-name domain-name))
   (str "GnuTLSKeyFile " (key-file-name domain-name))
   ""]
  )

(defn install-mod-gnutls
  []
  (actions/package "libapache2-mod-gnutls")
  (pallet.actions/exec
    {:language :bash}
    (stevedore/script
      ("a2dismod ssl")
      ("a2enmod gnutls"))
    )
  (actions/directory
    "/etc/apache2/ssl.crt"
    :owner "root"
    :group "root"
    :mode "700"
    )
  (actions/directory
    "/etc/apache2/ssl.key"
    :owner "root"
    :group "root"
    :mode "700"
    )
  (actions/remote-file
    "/etc/apache2/mods-available/gnutls.conf"
    :owner "root"
    :group "root"
    :mode "644"
    :force true
    :content 
    (string/join
      \newline
      gnutls-conf
      ))
  )