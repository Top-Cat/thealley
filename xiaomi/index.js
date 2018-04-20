const miio = require('miio');
var request = require('request');
var util = require('util');

//const BASE_URL = "http://thealley:5557/api";
//const BASE_URL = "http://localhost:13370/api";

miio.device({ token: '2e95785a0b8a5c1a5cbcf07accbb17f0', address: '10.48.8.86' })
	.then(onConnected)
	.catch(err => console.log(err));

function onConnected(dev) {
	const children = dev.children();
	for (const child of children) {
		child.on('movement', motionEvent.bind(null, child));
		child.on('voltageChanged', valueEvent.bind(null, child, 'voltage'));
		child.on('illuminanceChanged', valueEvent.bind(null, child, 'illuminance'));
	}
}

function valueEvent(ctx, prop, v) {
	console.log(prop, "changed to:", v);

	request.post({uri: process.env.BASE_URL + "/prop", json: {property: prop, val: v, sensor: ctx.id}, timeout: 500}, function (error, response, body) {
		console.log(JSON.stringify([String(error), String(response), String(body)]));
	});
}

function motionEvent(ctx) {
	console.log("movement!");

	request.post({uri: process.env.BASE_URL + "/motion", json: {sensor: ctx.id}, timeout: 500}, function (error, response, body) {
		console.log(JSON.stringify([String(error), String(response), String(body)]));
	});
}
