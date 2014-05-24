Confrere P2P Framework
==============================

Confrere is a P2P framework for java. It focus on providing the primitives needed to create a p2p application. 
Peers are organized in an overlay network using a routing scheme similar to Kademlia.

[Stream Control Transmission Protocol RFC4960] (http://tools.ietf.org/html/rfc4960)

##UDPMessageService##

###Ping###
args: Id _myId_  
Ping a peer to see if it is alive. Peer should respond with a Pong message.

###Pong###
args: Id myId  
Response to Ping message. Passes this Peers Id.

###GetPeers###
args: Id target  
ask for a list of peers closest to target Id. Peers with active connections should be noted so the requesting peer
knows which ones are likely to respond.

###Route###
args: Id dest, byte[] payload
request to route payload to destination address 



