var io = require('socket.io').listen(3000);

io.on('connection', function(socket){
	console.log("somebody connected");
  socket.on('test', function(data){
  	console.log("test: " + data);
  	console.log("test: " + data.id);
  	socket.emit('test', {id: data.id, status: 'success'});
  	socket.broadcast.emit('test2', data.data);
  });
});

