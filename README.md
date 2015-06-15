Confrere P2P Framework
==============================

Confrere is a P2P library which provides an overlay network on top of traditional
[Internet Prococol](https://en.wikipedia.org/wiki/Internet_Protocol). The main goal
of Confrere is to provide connectivity to all peers, even if they are in private networks
and behind NATs. Confrere provides two types of data transmission primitives: datagram, and
stream. Confrere uses only UDP packets.

In the Confrere network, each peer has its own unique 128-bit address. The address
is created randomly in a similar manner as [UUID](https://en.wikipedia.org/wiki/Universally_unique_identifier)
Peers join the Confrere network by finding other peers with similar address prefixes.
In this way, peers will build up a routing table similar to [Kademlia](https://en.wikipedia.org/wiki/Kademlia).

Datagram packets are routed on the Confrere network by sending to peers with addresses that closer to their
destination address. Like many other p2p networks, only O(log(n)) nodes are contacted for any message
to reach its destination of a total of n nodes in the network.

Stream connections are established between peers by first attempting to create a direct connection via
NAT hole punching/rendezvous technique. When a peer wishes to establish a stream connection with
another peer on the Confrere network, it will first send a datagram connection request to its
destination. If the destination peer accepts the connection, it will reply via datagram with its external
IP address. Now that both peers know each others IP address, a direct connection can be formed between them.


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

###Stream Connection###


## Protocol ##

Packet Header:

0
  0   1   2   3   4   5   6   7
+---+---+---+---+---+---+---+---+
|  Pkt Type     |  Chunk  Flags |
|               | R          I  |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
\                               \
/    Chunk Value                /
\                               \
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+


| ID value  | Chunk Type                   |
|-----------|------------------------------|
| 0         | Ping                         |
| 1         | Get Peers                    |
| 2         | Datagram Route               |
| 3         | Stream Data                  |


| Chunk Flags|   Meaning                    |
|------------+------------------------------|
| R (bit 4)  | Response bit. This pkt is a  |
|            | response from a previous     |
|            | request                      |
+-------------------------------------------+
| I (bit 0)  | IPv6 add address coding is   |
|            | IPv6                         |
--------------------------------------------+


##### IP address encoding #####

IPv4 socket addresses are encoded as:


0               4         6
+---------------+---------+
|  IP address   |   port  |
+-------------------------+


#### Ping ####

Ping messages are sent to determin if another peer is alive.
The originating Confrere ID and the destination's IP address
are attached as the body (Chunk Value) of the packet so that
each peer can learn of their own (external) IP address and
of the other peer's Confrere ID.

Request:

Chunk Value:
0               16                   32
+---------------+--------------------+
|  From ID      |   Dest IP Address  |
+---------------+--------------------+

Response:

Chunk Value:

0               16                   32
+---------------+--------------------+
|  From ID      |   Dest IP Address  |
+---------------+--------------------+

#### Get Peers ####

The Get Peers packet is sent to query another peer of their
routing table.

Request:

Chunk Value:
0               16
+---------------+
|  Target ID    |
+---------------+

Response:

Chunk Value:
0               16              32
+---------------+---------------+
|  Target ID    |  1st Peer ID  |
+---------------+---------------+
\  1st IP addr  /  2nd Peer ID  \
/             ....              /
\                               \
+-------------------------------+


##### Datagram #####

Chunk Value:
0               16              32
+---------------+---------------+
|  Target ID    |  Data         |
+---------------+---------------+
\             Data              \
/             ....              /
\                               \
+-------------------------------+



| 0         | Palyload data (DATA)         |
| 1         | Init (INIT)                  |
| 2         | (INIT ACK)                   |
| 3         | Selective Acknowledge (SACK) |

Chunk Flags




