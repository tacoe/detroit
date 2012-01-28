// detroit.js
// (c) el Thundros 2012

function Rand(debiel) {
	return Math.floor(Math.random() * debiel);
}

function GenHex(len) {
	var id = "", len = len || 8, chars="0123456789ABCDEF";
	for(var x=0; x<len;++x) {
		id += chars.substr(Rand(16), 1);
	}
	return id;
}


function World(viewCanvas) {
	var view = viewCanvas.getContext("2d"),
		self,
		lastMsg = 0,
		lastMsgTime = 0,
		sending = false,
		pips = {},
		localPip = "";
	
	var VIEW_WIDTH = 800, VIEW_HEIGHT = 600;
	
	function render() {
		view.fillStyle = "#e00000";
		view.fillRect(0,0, VIEW_WIDTH, VIEW_HEIGHT);

		view.fillStyle = "white";
		view.lineWidth = 3;
		for(var uid in pips) {
			var p = pips[uid];
			view.fillRect(p.loc.x - 2, p.loc.y - 2, 4, 4);
		}
	}
		
	function stepPhysics() {
		// handle impulses + movement
		render();
	}
	
	function stepNetwork() {
		sendMove(pips[localPip]);
	}

	function run() {
		setInterval(stepPhysics, 25);
		setInterval(stepNetwork, 100);
	}

	function spawnPip() {
		var pip = {
			id: GenHex(6),
 			loc: {x: Rand(VIEW_WIDTH), y: Rand(VIEW_HEIGHT)},
 			color: GenHex(6),
 			impulse: {x:0,y:0},
 			lastCmd: 0
		};
		pips[pip.id] = pip;
		localPip = pip.id;
		return pip;
	}
	
	var SERVER = "http://10.0.2.3:8080/";
	
	function receive(data) {
		var processed = 0;

		console.info("received", data);
		data.messages.forEach(function(msg) {
			lastMsg = msg.id;
			lastMsgTime = msg.timeStamp;

			if(msg.data.u && msg.data.u.length == 6) { // ignore Anne's test rommel
				var uid = msg.data.u;
				if(uid == localPip)
					return;
				if(! pips[uid])
					pips[uid] = {id:uid,loc:{x:0,y:0},impulse:{x:0,y:0}};
				if(msg.data.c == "mv") {
					pips[uid].loc.x = msg.data.x;
					pips[uid].loc.y = msg.data.y;
				}
				pips[uid].lastCmd = msg.id;
			}
			
			++processed;
		});

		console.info("processed " + processed + "messages");
		console.info("pips", pips);
	}
	
	function send(data) {
		if(sending) {
			console.info("still sending, skipping");
			return;
		}
		sending = true;
		
		var url = SERVER + "?since=" + lastMsg + "&j=" + encodeURIComponent(JSON.stringify(data));

		$.ajax(url, {
			type: "GET", async: true,
			error: function() { console.warn("Failed to send"); sending = false; },
			success: function(data, status, xhr) {
				sending = false;
				receive(data);
			},
			complete: function() { sending = false; }
		});
	}
	
	function sendMove(pip) {
		send({"c": "mv", "u": pip.id, "x": pip.loc.x, "y": pip.loc.y});
	}
	
	return self = { spawnPip: spawnPip, run: run };
}


// {"cmd":"read","uid":"D1D85248","eid":0} // start, eid = starting event id to read
// {"cmd":"move","uid":"D1D85248","x":0,"y":0} // change position, x = 0..799, y = 0..699
// {"cmd":"eat","uid":"D1D85248","cid":1} // eat candy #cid