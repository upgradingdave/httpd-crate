; Copyright (c) Michael Jerger, Dave Paroulek. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software.

;; Convenience functions for creating apche vhost config files.

(ns httpd.crate.vhost
  (:require [clojure.string :as string]
            [httpd.crate.common :as common]
            [httpd.crate.mod-gnutls :as gnutls]
            [httpd.crate.mod-rewrite :as rewrite]))

(defn vhost-server-alias
  "Define aliases. For example if your domain-name is example.com, you
  might want to pass [\"www.example.com\" \"ftp.example.com\"]"
  [& [domain-names]]
  (if domain-names
    [(apply str "ServerAlias " (interpose " " domain-names))]
    [])
  )

(defn vhost-head
  "listening spec may be: x.x.x.x:443 [x6:x6:x6:x6:x6:x6:x6:x6]:443"
  [& {:keys [listening-spec
             listening-interface
             listening-port
             domain-name 
             server-admin-email
             aliases]
      :or {listening-interface "*"
           listening-port "80"}}]
  (let [used-server-admin-email 
        (if server-admin-email
          server-admin-email
          (str "admin@" domain-name))]
    (into 
      [(if listening-spec
         (str "<VirtualHost " listening-spec ">")
         (str "<VirtualHost " listening-interface ":" listening-port ">"))]
      (common/prefix 
        "  " 
        (into []
              (concat 
                [(str "ServerName " domain-name)]
                (vhost-server-alias aliases)
                [(str "ServerAdmin " used-server-admin-email)
                 ""])))
      )
  ))

(def vhost-tail ["</VirtualHost>"])

(defn vhost-document-root
  [& [document-root-path]]
  (if document-root-path
    [(str "DocumentRoot \"" document-root-path "\"")
     ""]
    [])  
  )

(defn vhost-directory
  [file-path & {:keys [directory-options]
                :or {directory-options
                     ["Order allow,deny"
                      "Allow from all"]}}]
  (into 
    []
    (concat
      [(str "<Directory \"" file-path "\">")]
      (common/prefix 
          "  "
          directory-options)
      ["</Directory>"
       ""]
      ))
  )

(defn vhost-location
  "If path is nil, defaults to \"/\" "
  [& {:keys [path
             location-options]
      :or {path "/"
           location-options
           ["Order allow,deny"
            "Allow from all"]}}]
   (into []
        (concat
          [(str "<Location " path ">")]
          (common/prefix 
            "  "
            location-options)
          ["</Location>"
          ""]))
   )

(defn vhost-log 
  [& {:keys [domain-name
             error-name
             log-name
             log-format]
      :or {error-name "error"
           log-name "access_log"
           log-format "common"}}]
  (let [log-prefix (if domain-name
                     (str domain-name "-")
                     "")]
    [(str "ErrorLog \"/var/log/apache2/" log-prefix error-name "\"")
      "LogLevel warn"
      (str "CustomLog \"/var/log/apache2/" log-prefix log-name "\" " log-format)
      ""]
    )
   )

(defn vhost-conf-default-redirect-to-https-only
  "Just redirect http request permanently to https"
  [& {:keys [domain-name 
             aliases 
             server-admin-email 
             document-root-path 
             port]
      :or {server-admin-email "your-name@your-domain.com"
           port "80"}}]
  (into 
    []
    (concat
      (vhost-head :listening-port port 
                  :domain-name domain-name 
                  :server-admin-email server-admin-email
                  :aliases aliases)
      (common/prefix 
          "  "
          (into 
            []
            (concat
              (vhost-document-root document-root-path)
              (vhost-log :error-name "error.log"
                         :log-name "access.log"
                         :log-format "combined")
              (rewrite/vhost-rewrite-rules 
                ["RewriteCond %{HTTPS} !on"
                 "RewriteRule ^/(.*)$ https://%{SERVER_NAME}/$1 [R=301,L]"]
                :use-proxy false))))
      vhost-tail
      )
    )
  )

(defn vhost-conf-ssl-default
  "a https default configuration"
  [& {:keys [domain-name 
             aliases 
             server-admin-email 
             document-root-path 
             port
             ssl-module]
      :or {server-admin-email "your-name@your-domain.com"
           port "443"
           ssl-module :gnutls}}]
  (into 
    []
    (concat
      (vhost-head :listening-port port
                  :domain-name domain-name 
                  :server-admin-email server-admin-email
                  :aliases aliases)
      (common/prefix 
          "  "
          (into 
            []
            (concat
              (vhost-document-root document-root-path)
              (vhost-log :error-name "error.log"
                 :log-name "ssl-access.log"
                 :log-format "combined")
              (if (= ssl-module :gnutls)
                (gnutls/vhost-gnutls domain-name)
                ))))   
      vhost-tail
      )
    )
  )

(defn vhost-conf-default
  "Most of my apache vhosts are for java apps. Here's what I usually use."
  [domain-name server-admin-email document-root-path aliases port]
  (into 
    []
    (concat
      (vhost-head :domain-name domain-name 
                  :server-admin-email server-admin-email
                  :aliases aliases)
      (common/prefix 
          "  "
          (into 
            []
            (concat
              (vhost-document-root document-root-path)
              (vhost-log :domain-name domain-name)
              (vhost-location :path "/")
              (vhost-directory document-root-path)
              (rewrite/vhost-rewrite-rules 
                [(str "RewriteRule ^/$ http://localhost:" port "/ [P]")
                 (str "RewriteRule ^/(.+)$ http://localhost:" port "/$1 [P]")])
              )))
      vhost-tail
      )
    )
  )
  
(defn vhost-conf
  "Generate a vhost config. domain-name will be used to name the vhost
  conf file as well as log files. If you need to set up complicated
  vhost, pass a string of xml to :vhost-xml and you will have full
  control over what the vhost file looks like"
  [domain-name & [{:keys [server-admin-email document-root-path 
                          vhost-xml aliases port]
                   :as opts
                   :or {server-admin-email "your-name@your-domain.com"
                        port "3000"}}]]
  (or 
    vhost-xml 
    (string/join
      \newline
      (vhost-conf-default 
        domain-name 
        server-admin-email 
        document-root-path 
        aliases port)
      )
    )
  )

