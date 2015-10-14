; Copyright (c) Michael Jerger, Dave Paroulek. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software.
;

(ns httpd.crate.webserver-maintainance
 (:require
  [clojure.string :as cloj-str] 
  [httpd.crate.mod-jk :as jk]            
  [pallet.actions :as actions]))

;default maintainance error page content
(def var-www-static-error-503-html
  ["<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">"
   "<html>"
   "<head>"
   "<title>Maintainance</title>"
   "<meta name=\"ROBOTS\" content=\"NOINDEX, NOFOLLOW\">"
   "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">"
   "<meta http-equiv=\"content-type\" content=\"application/xhtml+xml; charset=UTF-8\">"
   "<meta http-equiv=\"content-style-type\" content=\"text/css\">"
   "<meta http-equiv=\"expires\" content=\"0\">"
   "        <style type=\"text/css\">"
   "                * {background-color: #EEF0F2}"
   "        </style>"
   "</head>"
   "<body>"
   "        <center>"
   "                <h1>We thank you for your interest! </h1>"
   "                <h2>The site is temporarily unavailable due to maintenance. </h2>"
   "                <p>Thank you for your understanding and your patience! </p>"
   "        </center>"
   "</body>"
   "</html>"
   ]
  )



(defn write-maintainance-file
   [& {:keys [content]
    :or {content var-www-static-error-503-html}}]
   (actions/directory
     "/var/www/static/error"
     :action :create
     :mode "644"
     :owner "root"
     :group "www-data")
  (actions/remote-file
    "/var/www/static/error/503.html"
    :action :create
    :overwrite-changes true
    :mode "644"
    :literal true
    :owner "root" 
    :group "www-data"
    :content (cloj-str/join
               \newline
               content
               )
    )
  )

(defn vhost-service-unavailable-error-page
 [& {:keys [consider-jk]
    :or {consider-jk false}}]
 (into 
  []
  (concat
   [(str "ErrorDocument 503 " "/error/503.html")
   "Alias /error \"/var/www/static/error\""]
   (if consider-jk 
    (jk/vhost-jk-unmount :path "/error/*")
    [])
   [""]
   ))
 )
