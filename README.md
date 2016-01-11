# UNOFFICAL i2b2 repository
Unofficial repo of i2b2 source releases from 1.0 through 1.7.04

This repository is something that I put together from the i2b2 source releases, removing as much junk as I could,
and attempting to align them with what I guessed the source repo might look like.

There is an official repository of the current sources which is available at https://github.com/i2b2/i2b2-core-server
and related repos.  The official repos should be preferred for most uses.

Below is the mapping between the structure here and the structure of the newly released official repos.

 - Server - https://github.com/i2b2/i2b2-core-server
 - Server/webclient - https://github.com/i2b2/i2b2-webclient
 - Client - https://github.com/i2b2/i2b2-workbench
 - Common - https://github.com/i2b2/i2b2-workbench/tree/master/edu.harvard.i2b2.common

The two different commons may require a little explanation.  There was originally only one, shared by the server
and the Eclipse client (ie workbench), but it was cloned and allowed to diverge a few versions ago.  I'd hope that they'll
one day get merged back together again.
