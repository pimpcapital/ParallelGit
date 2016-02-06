app.service('NotificationService', function(notify) {

  notify.config({
    duration: 5000,
    position: 'right',
    templateUrl: 'system/notification.html'
  });

  this.info = function(msg) {
    notify({
      message: msg
    });
  };


});