app.directive('labelNoCheckout', function() {
  return {
    restrict: 'E',
    scope: false,
    template: '<span class="label label-success" uib-popover-template="\'templates/label-no-checkout.html\'" popover-placement="right" popover-trigger="mouseenter">No Checkout</span>'
  }
});

app.directive('labelCheckout', function() {
  return {
    restrict: 'E',
    scope: false,
    template: '<span class="label label-danger" uib-popover-template="\'templates/label-checkout.html\'" popover-placement="right" popover-trigger="mouseenter">Checkout Required</span>'
  }
});