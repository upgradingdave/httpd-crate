(ns httpd.crate.config)

;; Convenience functions for creating apche configuration files.
;; So far, there's only functions for generating vhost configs, but
;; eventually hope to evolve to cover all apache configs.

(defn vhost-server-alias
  "Define aliases. For example if your domain-name is example.com, you
  might want to pass [\"www.example.com\" \"ftp.example.com\"]"
  [& [domain-names]]
  (if domain-names
    (apply str "ServerAlias " (interpose " " domain-names))))

(defn vhost-directory
  [& [filepath]]
  (if filepath
    (str
     "<Directory \"" filepath "\">" \newline
     "  Order allow,deny " \newline
     "  Allow from all" \newline
     "</Directory>" \newline)))

(defn vhost-location
  "If path is nil, defaults to \"/\" "
  [& [path]]
  (str
   "<Location " (or path "/") ">" \newline
   "  Order allow,deny " \newline
   "  Allow from all" \newline
   "</Location>" \newline ))

(defn vhost-log 
  [domain-name]
  (str
   "ErrorLog \"/var/log/apache2/" domain-name "-error\"" \newline
   "LogLevel warn" \newline
   "CustomLog \"/var/log/apache2/" domain-name "-access_log\" common"
   \newline))

(defn vhost-rewrite-rules
  "Define the rewrite rules for a VirtualHost. Pass a vector of
  rewrite rules like this:

  :rewrite-rules [\"RewriteRule ^/$ http://test.com:8080 [P]\"
                  \"RewriteRule ^/login$ http://test.com [P]\"]
"
  [& [rewrite-rules]]
  (if rewrite-rules
    (str "ProxyRequests On" \newline
         "RewriteEngine On" \newline
         \newline
         (apply str (interpose \newline rewrite-rules))
         \newline)))

(defn vhost-conf-default
  "Most of my apache vhosts are for java apps. Here's what I usually use."
  [domain-name server-admin-email document-root-path aliases port]
  (str "<VirtualHost *:80>" \newline
       "ServerName " domain-name \newline
       (vhost-server-alias aliases)
       "ServerAdmin " server-admin-email \newline
       (if document-root-path
         (str "DocumentRoot \"" document-root-path "\"" \newline))
       \newline
       (vhost-log domain-name)
       \newline
       (vhost-location "/")
       \newline
       (vhost-directory document-root-path)
       \newline
       (vhost-rewrite-rules 
        [(str "RewriteRule ^/$ http://localhost:" port "/ [P]")
         (str "RewriteRule ^/(.+)$ http://localhost:" port "/$1 [P]")])
       \newline
       "</VirtualHost>" \newline))

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
  (or vhost-xml (vhost-conf-default domain-name server-admin-email 
                                    document-root-path aliases port)))

