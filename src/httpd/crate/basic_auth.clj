; Copyright (c) Michael Jerger. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software.

(ns httpd.crate.basic-auth
  (require 
    [clojure.string :as string]
    [pallet.actions :as actions]
    [pallet.stevedore :as stevedore]
    ))

(defn- htpasswd-file-name
  [domain-name]
  (str "/etc/apache2/htpasswd-" domain-name)
  )

(defn configure-basic-auth-user-credentials
  "For passwordgeneration use htpasswd -nbs user passwd"
  [ & {:keys [domain-name
              user-credentials]}]
  {:pre [(not (nil? domain-name))
         (not (nil? user-credentials))
         (vector? user-credentials)]}
  (actions/remote-file
    (htpasswd-file-name domain-name)
    :owner "root"
    :group "www-data"
    :mode "640"
    :literal true
    :force true
    :content
    (string/join
      \newline 
      user-credentials))
  )

(defn vhost-basic-auth-options
  [ & {:keys [domain-name]
       }]
  {:pre [(not (nil? domain-name))]}
  ["Deny from all"
   (str "AuthUserFile " (htpasswd-file-name domain-name))
   (str "AuthName \"Authorization for " domain-name "\"")
   "AuthType Basic"
   "Satisfy Any"
   "require valid-user"]  
  )