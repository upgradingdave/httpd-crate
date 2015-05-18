; Copyright (c) Michael Jerger, Dave Paroulek. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software.
;

(ns httpd.crate.config)

(def ^:dynamic limits
  ["ServerLimit 150"
   "MaxClients  150"])

(def ^:dynamic loadtest-logging
  ["# Format is: [remote host] [remote logname] [remote user] [request time] \"[first request line]\" [status]" 
   "# [respionse size in bytes] \"[referer]\" \"[user agent]\" [processtime in microseconds]"
   "LogFormat \"%h %l %u %t \\\"%r\\\" %>s %b \\\"%{Referer}i\\\" \\\"%{User-agent}i\\\" %D\" loadtest"
  ""])

(def ^:dynamic security
  ["ServerTokens Prod"
   "ServerSignature On"
   "TraceEnable Off"
   "Header set X-Content-Type-Options: \"nosniff\""
   "Header set X-Frame-Options: \"sameorigin\""
])

(def ^:dynamic ports
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