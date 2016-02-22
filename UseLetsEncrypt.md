Tested at ubuntu14.04

git clone https://github.com/letsencrypt/letsencrypt
cd letsencrypt
service apache2 stop
./letsencrypt-auto certonly --standalone --email admin@meissa-gmbh.de -d jira.meissa-gmbh.de
service apache2 start

-> /etc/letsencrypt/live/jira.meissa-gmbh.de/fullchain.pem. Your cert


GnuTLSCertificateFile /etc/letsencrypt/live/jira.meissa-gmbh.de/cert.pem
GnuTLSKeyFile /etc/letsencrypt/live/jira.meissa-gmbh.de/privkey.pem
GnuTLSClientCAFile /etc/letsencrypt/live/jira.meissa-gmbh.de/fullchain.pem