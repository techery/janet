var io = require('socket.io').listen(3000);

io.on('connection', function(socket){
	console.log("somebody connected");
  socket.on('test', function(data){
  	console.log("test: " + JSON.stringify(data));
//  	socket.emit('test', {id: data.id, status: 'success'});
  	io.emit('test2', data.data);
  });
});

