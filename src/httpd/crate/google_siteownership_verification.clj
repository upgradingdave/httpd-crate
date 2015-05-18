; Copyright (c) Michael Jerger, Dave Paroulek. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software.
;

(ns httpd.crate.google-siteownership-verification
  (:require 
    [httpd.crate.mod-jk :as jk]            
    [pallet.actions :as actions]))

(defn vhost-ownership-verification
  [& {:keys [id consider-jk]
      :or {consider-jk false}}]
  (into 
    []
    (concat
      [(str "Alias /google" id ".html \"/var/www/static/google/google" id ".html\"")]
      (if consider-jk 
        (jk/vhost-jk-unmount :path (str "/google" id ".html"))
        [])
      [""]
      )))

(defn configure-ownership-verification
  [{:keys [id]}]
  (actions/directory
    "/var/www/static/google"
    :path true
    :owner "www-data"
    :group "www-data"
    :mode "774")
  (actions/remote-file
    (str "/var/www/static/google/google" id ".html")
    :owner "www-data"
    :group "www-data"
    :mode "644"
    :force true
    :content 
    (str "google-site-verification: google" id ".html"))
  )