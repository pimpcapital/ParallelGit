app.service('Notification', function(notify) {

  notify.config({
    position: 'right',
    templateUrl: 'system/notification.html'
  });

  this.info = function(msg) {
    notify({
      message: msg,
      duration: 5000
    });
  };

  this.error = function(msg) {
    notify({
      message: msg,
      duration: 0,
      classes: ['cg-notify-message-error']
    });
  }

});