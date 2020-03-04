# SSH Library for eXist-db

This library is a wrapper around the JSch library.

There are two functions defined.  One is to establish an SSH session and the other is to setup port forwarding once the session is established.


```xquery
xquery version "3.1";

import module namespace ssh = "https://exist-db.org/exist-db/ns/app/jsch";

ssh:get-session("10.211.55.15", "parallels", "xxxxxxxx")
```

This returns an xs:long that is a handle to the session.

```xquery
xquery version "3.1";

import module namespace ssh = "https://exist-db.org/exist-db/ns/app/jsch";

let $session := ssh:get-session("10.211.55.15", "parallels", "xxxxxxxx")

return ssh:forward-port($session, 5656, "secure.example.com", 3306)
```