; Copyright (c) Michael Jerger. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software.

(ns httpd.crate.apache2
  (:require 
    [clojure.string :as string]
    [pallet.actions :as actions]
    [pallet.stevedore :as stevedore]))

(def limits
  ["ServerLimit 150"
   "MaxClients  150"])

(def loadtest-logging
  ["# Format is: [remote host] [remote logname] [remote user] [request time] \"[first request line]\" [status]" 
   "# [respionse size in bytes] \"[referer]\" \"[user agent]\" [processtime in microseconds]"
   "LogFormat \"%h %l %u %t \\\"%r\\\" %>s %b \\\"%{Referer}i\\\" \\\"%{User-agent}i\\\" %D\" loadtest"
  ""])

(defn security
  [ & {:keys [allowed-methods ]
       :or {allowed-methods "GET POST OPTIONS"}}]
  ["<Directory />"
   "  Options FollowSymLinks"
   "  AllowOverride FileInfo"
   "  Require all granted"
   "</Directory>"
   ""
   ;;TODO - tut nicht
   ;(str "<LimitExcept " allowed-methods ">")
   ;"</LimitExcept>"
   ;""                                                                                                                                                                                                                                            
   "ServerTokens Prod"                                                                                                                                                                                                    
   "ServerSignature On"                                                                                                                                                                                                   
   "TraceEnable Off"
   "Header set X-Content-Type-Options: \"nosniff\""
   "Header set X-Frame-Options: \"sameorigin\""
])

(def ports
  ["Listen 80"
   ""
   "<IfModule mod_ssl.c>"
   "  Listen 443"
   "</IfModule>"
   ""
   "<IfModule mod_gnutls.c>"
   "  Listen 443"
   "</IfModule>"
   ""])

(defn- configure-file
  [file-name content]
  (actions/remote-file
    file-name
    :owner "root"
    :group "root"
    :mode "644"
    :force true
    :content 
    (string/join
      \newline
      content
      ))
  )
  
(defn- configure-file-and-enable
  [file-name link-to-enable content]
  (configure-file file-name content)
  (actions/symbolic-link 
    file-name 
    link-to-enable
    :action :create)
  )

(defn config-apache2-production-grade
  [ & {:keys [limits 
              security 
              ports]
       :or {limits limits
            security (security)
            ports ports}}]
  (configure-file-and-enable "/etc/apache2/conf-available/limits.conf"
                             "/etc/apache2/conf-enabled/limits.conf"
                             limits)
  (configure-file-and-enable "/etc/apache2/conf-available/security.conf"
                             "/etc/apache2/conf-enabled/security.conf"
                             security)  
  (configure-file "/etc/apache2/ports.conf" ports)
  (pallet.actions/exec
      {:language :bash}
      (stevedore/script
        ("a2enmod headers")
      ))
  )

(defn config-and-enable-vhost
  [vhost-name vhost-content]
  (configure-file 
    (str "/etc/apache2/sites-available/" vhost-name ".conf") 
    vhost-content)
   (pallet.actions/exec
      {:language :bash}
      (stevedore/script
        ((str "a2ensite " ~vhost-name))
      ))  
  )

(defn install-apache2
  []
  (actions/package "apache2")
  (actions/package "apachetop")
  (pallet.actions/exec
      {:language :bash}
      (stevedore/script
        ("a2enmod rewrite")
      ))
  )
