app.service('Dialog', function($uibModal) {

  var me = this;

  me.confirm = function(title, message) {
    return $uibModal.open({
      templateUrl: 'system/confirm-dialog-modal.html',
      size: 'sm',
      resolve: {
        title: function() {
          return title;
        },
        message: function() {
          return message;
        }
      },
      controller : 'ConfirmDialogController'
    }).result;
  };

  me.prompt = function(title, fields) {
    return $uibModal.open({
      templateUrl: 'system/prompt-dialog-modal.html',
      size: 'sm',
      resolve: {
        title: function() {
          return title;
        },
        fields: function() {
          return fields;
        }
      },
      controller : 'PromptDialogController'
    }).result;
  };

  me.select = function(title, model, data) {
    return $uibModal.open({
      templateUrl: 'system/select-dialog-modal.html',
      size: 'lg',
      resolve: {
        title: function() {
          return title;
        },
        model: function() {
          return model;
        },
        data: function() {
          return data;
        }
      },
      controller : 'SelectDialogController'
    }).result;
  }

});