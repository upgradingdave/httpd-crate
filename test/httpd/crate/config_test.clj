(ns httpd.crate.config-test
  (:require
    [clojure.test :refer :all]
    [pallet.actions :as actions]
    [httpd.crate.config :as sut]
    ))

(deftest vhost-conf
  (testing 
    "original conf remains unchanged"
    (is 
      (= ["<VirtualHost *:80>"
          "ServerName owncloud.politaktiv.org"
          "ServerAlias alias1 alias2"
          "ServerAdmin admin@politaktiv.org"
          ""
          "DocumentRoot \"/document-root\""
          ""
          "ErrorLog \"/var/log/apache2/owncloud.politaktiv.org-error\"" 
          "LogLevel warn" 
          "CustomLog \"/var/log/apache2/owncloud.politaktiv.org-access_log\" common"
          ""
          "<Location />" 
          "  Order allow,deny" 
          "  Allow from all" 
          "</Location>"
          ""
          "<Directory \"/document-root\">"
          "  Order allow,deny"
          "  Allow from all"
          "</Directory>" 
          ""
          "ProxyRequests on" 
          "RewriteEngine on" 
          "RewriteRule ^/$ http://localhost:80/ [P]" 
          "RewriteRule ^/(.+)$ http://localhost:80/$1 [P]" 
          "" 
          "</VirtualHost>"]
         (sut/vhost-conf-default 
           "owncloud.politaktiv.org" 
           "admin@politaktiv.org" 
           "/document-root"
           ["alias1" "alias2"]
           "80" )         
         ))
    )
  
  (testing 
    "http -> https"
    (is 
      (= ["<VirtualHost *:80>"
          "ServerName owncloud.politaktiv.org"
          "ServerAdmin admin@politaktiv.org"
          ""          
          "ErrorLog \"/var/log/apache2/error\"" 
          "LogLevel warn" 
          "CustomLog \"/var/log/apache2/access_log\" common"
          ""
          "RewriteEngine on"
          "RewriteCond %{HTTPS} !on"
          "RewriteRule ^/(.*)$ https://%{SERVER_NAME}/$1 [R=301,L]"
          ""
          "</VirtualHost>"] 
         (sut/vhost-conf-default-redirect-to-https-only
           "owncloud.politaktiv.org" 
           "admin@politaktiv.org" 
           nil 
           nil 
           80 )
         ))    
    )
  )
  