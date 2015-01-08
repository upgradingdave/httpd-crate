;;; Pallet project configuration file

(require
 '[httpd.groups.httpd :refer [httpd]])

(defproject httpd
  :provider {:vmfest
             {:node-spec
              {:image {:image-id :ubuntu-14.04}}}}
  :groups [httpd])
