; Copyright (c) Michael Jerger, Dave Paroulek. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software.

(ns httpd.crate.common)

(defn prefix
  "prefixes each string contained in lines with indent."
  ([indent lines]
    (prefix indent lines ())
  )
  ([indent lines result]
    {:pre [(list? result)]}
    (if (empty? lines)
      (into [] result)
      (recur 
        indent
        (pop lines)
        (conj result 
              (str indent
                   (peek lines)))
              )
      ))
  )