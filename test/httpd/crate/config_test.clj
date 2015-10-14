; Copyright (c) Michael Jerger. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software.

(ns httpd.crate.config-test
  (:require
    [clojure.test :refer :all]
    [pallet.actions :as actions]
    [httpd.crate.vhost :as vhost]
    [httpd.crate.config :as config]
    ))

(deftest vhost-head
  (testing
    "default admins emailadress"
    (is 
      (= ["<VirtualHost *:80>"
          "  ServerName owncloud.politaktiv.org"
          "  ServerAdmin admin@owncloud.politaktiv.org"
          "  "]
         (vhost/vhost-head
           :domain-name "owncloud.politaktiv.org")
         )
      ))
  )

(deftest vhost-conf
  (testing 
    "original conf remains as unchanged as possible"
    (is 
      (= ["<VirtualHost *:80>"
          "  ServerName owncloud.politaktiv.org"
          "  ServerAlias alias1 alias2"
          "  ServerAdmin admin@politaktiv.org"
          "  "
          "  DocumentRoot \"/document-root\""
          "  "
          "  ErrorLog \"/var/log/apache2/owncloud.politaktiv.org-error\"" 
          "  LogLevel warn" 
          "  CustomLog \"/var/log/apache2/owncloud.politaktiv.org-access_log\" common"
          "  "
          "  <Location />" 
          "    Order allow,deny" 
          "    Allow from all" 
          "  </Location>"
          "  "
          "  <Directory \"/document-root\">"
          "    Order allow,deny"
          "    Allow from all"
          "  </Directory>" 
          "  "
          "  ProxyRequests on" 
          "  RewriteEngine on" 
          "  RewriteRule ^/$ http://localhost:80/ [P]" 
          "  RewriteRule ^/(.+)$ http://localhost:80/$1 [P]" 
          "  " 
          "</VirtualHost>"]
         (vhost/vhost-conf-default 
           "owncloud.politaktiv.org" 
           "admin@politaktiv.org" 
           "/document-root"
           ["alias1" "alias2"]
           "80" )         
         ))
    )
  
  (testing 
    "a simple https config"
    (is 
      (= ["<VirtualHost *:443>"
          "  ServerName jira.intra.politaktiv.org"
          "  ServerAdmin webmaster@politaktiv.org"
          "  "
          "  ErrorLog \"/var/log/apache2/error.log\"" 
          "  LogLevel warn" 
          "  CustomLog \"/var/log/apache2/ssl-access.log\" combined"
          "  "
          "  GnuTLSEnable on"
          "  GnuTLSCacheTimeout 300"
          "  GnuTLSPriorities SECURE:!VERS-SSL3.0:!MD5:!DHE-RSA:!DHE-DSS:!AES-256-CBC:%COMPAT"
          "  GnuTLSExportCertificates on"
          "  "
          "  GnuTLSCertificateFile /etc/apache2/ssl.crt/jira.intra.politaktiv.org.certs"
          "  GnuTLSKeyFile /etc/apache2/ssl.key/jira.intra.politaktiv.org.key"
          "  "
          "</VirtualHost>"
          ]
         (vhost/vhost-conf-ssl-default 
           :domain-name  "jira.intra.politaktiv.org" 
           :server-admin-email "webmaster@politaktiv.org"
           :ssl-module :gnutls)         
         ))
    )
  
  (testing 
    "http -> https"
    (is 
      (= ["<VirtualHost *:80>"
          "  ServerName owncloud.politaktiv.org"
          "  ServerAdmin admin@politaktiv.org"
          "  "
          "  ErrorLog \"/var/log/apache2/error.log\"" 
          "  LogLevel warn" 
          "  CustomLog \"/var/log/apache2/access.log\" combined"
          "  "
          "  RewriteEngine on"
          "  RewriteCond %{HTTPS} !on"
          "  RewriteRule ^/(.*)$ https://%{SERVER_NAME}/$1 [R=301,L]"
          "  "
          "</VirtualHost>"] 
         (vhost/vhost-conf-default-redirect-to-https-only
           :domain-name "owncloud.politaktiv.org" 
           :server-admin-email "admin@politaktiv.org")
         ))    
    )
  )

(deftest vhost-directory
  (testing 
    "with additional options"
    (is 
      (= ["<Directory \"/var/www/owncloud/\">"
          "  Options Indexes FollowSymLinks MultiViews"
          "  AllowOverride All"
          "  SetEnv MOD_X_SENDFILE_ENABLED 1"
          "  XSendFile On"
          "  Order allow,deny"
          "  allow from all"
          "</Directory>"
          ""]
         (vhost/vhost-directory 
           "/var/www/owncloud/" 
           :directory-options 
           ["Options Indexes FollowSymLinks MultiViews"
            "AllowOverride All"
            "SetEnv MOD_X_SENDFILE_ENABLED 1"
            "XSendFile On"
            "Order allow,deny"
            "allow from all"])
         ))
    )
  )

(deftest vhost-location
  (testing 
    "with additional options acording to newer apache config"
    (is 
      (= ["<Location />"
          "  Satisfy Any"
          "  Order deny,allow"
          "  Allow from all"
          "</Location>"
          ""]
         (vhost/vhost-location  
           :location-options 
           ["Satisfy Any"
            "Order deny,allow"
            "Allow from all"])
         ))
    )
  )

(deftest limits
  (testing 
    "with additional options acording to newer apache config"
    (is 
      (= ["ServerLimit 150" 
          "MaxClients  150"]
         (config/limits)
         ))
    ))