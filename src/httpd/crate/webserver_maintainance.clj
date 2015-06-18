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

(defn vhost-service-unavailable-error-page
 [& {:keys [error-html-file consider-jk]
    :or {consider-jk false
        error-html-file "/error/503.html"}}]
 (into 
  []
  (concat
   [(str "ErrorDocument 503 " error-html-file)
   "Alias /error \"/var/www/static/error\""]
   (if consider-jk 
    (jk/vhost-jk-unmount :path "/error/*")
    [])
   [""]
   )))

(defn install-maintainance-default-page-var-www
  []
  (actions/remote-file
    "/var/www/static/error/503.html"
    :mode "644"
    :literal true
    :owner "root" 
    :group "www-data"
    :content (cloj-str/join
               \newline
               ["<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">"
                "<html>"
                "<head>"
                "<title>politaktiv.org in Wartung</title>"
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
                "                <h1> Wir danken Ihnen für Ihr Interesse! </h1>"
                "                <img src=politaktiv_logo.png>"
                "                <h2>Leider ist PolitAktiv wegen Wartungsarbeiten kurzfristig nicht erreichbar. </h2>"
                "                Wartungsarbeiten an "
                "                unserer Seite finden üblicherwiese <u>Freitags von 8:00Uhr-10:00Uhr</u> statt.<br>"
                "                <br>"
                "                Falls Sie sich über PolitAktiv informieren wollen, <br>"
                "                besuchen Sie bitte "
                "                das <a href=\"http://www.humanithesia.org/index.php/edemocracy/politaktiv.html\">"
                "                        Diskussionsportal der Integrata-Stiftung "
                "                    </a>"
                "                die das Projekt \"PolitAktiv\" leitet. <br>"
                "                <br>"
                "                Vorschläge zur Seite oder dem Projekt an sich sind im"
                "                <a href=\"http://www.humanithesia.org/index.php/forum-a/29-edemocracy-allgemein.html\">"
                "                dortigen Forum"
                "                </a> sehr willkommen.<br>"
                "                <br>"
                "                Vielen Dank für Ihr Verständnis!"
                "        </center>"
                "</body>"
                "</html>"
                ]
               )
    )
  )
