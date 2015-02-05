; Copyright (c) Michael Jerger. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software.

(ns httpd.crate.mod-proxy-http
  (require 
    [clojure.string :as string]
    [pallet.actions :as actions]
    [pallet.stevedore :as stevedore]
    ))

(defn vhost-proxy
  [& {:keys [target-host
             target-port]
      :or {target-host "localhost"
           target-port "8080"}}]
  ["<IfModule mod_proxy.c>"
   (str "  ProxyPass / http://" target-host ":" target-port "/")
   (str "  ProxyPassReverse / http://" target-host ":" target-port "/")
   "  ProxyPreserveHost On"
   "</IfModule>"]
  )

(defn install-mod-proxy-http
  []
  (actions/package "libapache2-mod-proxy-html")
  (pallet.actions/exec
    {:language :bash}
    (stevedore/script
      ("a2enmod proxy_http")
      ("a2enmod proxy"))
    )
  )