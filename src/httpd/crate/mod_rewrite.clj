; Copyright (c) Michael Jerger. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software.

(ns httpd.crate.mod-rewrite
  (require
    [httpd.crate.cmds :as cmds]))

(defn install-mod-rewrite
  []
  (cmds/a2enmod "rewrite")
  )

(defn vhost-rewrite-rules
  "Define the rewrite rules for a VirtualHost. Pass a vector of
  rewrite rules like this:

  :rewrite-rules [\"RewriteRule ^/$ http://test.com:8080 [P]\"
                  \"RewriteRule ^/login$ http://test.com [P]\"]
"
  [rewrite-rules 
   &{:keys [use-proxy]
        :or {use-proxy true}}]
  (if rewrite-rules
    (concat
      (if use-proxy 
        ["ProxyRequests on"]
        [])
       ["RewriteEngine on"]
      rewrite-rules
      [""])
    []
    )
  )