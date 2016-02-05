/*
 * angular-ui-tab-scroll
 * https://github.com/VersifitTechnologies/angular-ui-tab-scroll
 *
 * Version: 2.1.3
 * License: MIT
 */

angular.module('ui.tab.scroll', [])
    .provider('scrollableTabsetConfig', function(){

          //the default options
          var defaultConfig = {
            showDropDown: true,
            showTooltips: true,
            tooltipLeftPlacement: 'right',
            tooltipRightPlacement: 'left',
            scrollBy: '50',
            autoRecalculate: false
          };

          var config = angular.extend({}, defaultConfig);

          return {
            setShowDropDown : function(value){
              config.showDropDown = (value == true);
            },
            setShowTooltips : function(value){
              config.showTooltips = (value == true);
            },
            setTooltipLeftPlacement : function(value){
              config.tooltipLeftPlacement = value;
            },
            setTooltipRightPlacement : function(value){
              config.tooltipRightPlacement = value;
            },
            setScrollBy : function(value){
              config.scrollBy = value;
            },
            setAutoRecalculate : function(value){
              config.autoRecalculate = (value == true);
            },
            $get: function(){
              return {
                showDropDown: config.showDropDown,
                showTooltips: config.showTooltips,
                tooltipLeftPlacement: config.tooltipLeftPlacement,
                tooltipRightPlacement: config.tooltipRightPlacement,
                scrollBy: config.scrollBy,
                autoRecalculate: config.autoRecalculate
              };
            }
          };
        }
    )
    .directive('scrollableTabset', [
      'scrollableTabsetConfig', '$window', '$interval', '$timeout','$sce',
      function(scrollableTabsetConfig, $window, $interval, $timeout, $sce) {

        return {
          restrict: 'AE',
          transclude: true,

          scope: {
            showDropDown: '@',
            showTooltips: '@',
            tooltipLeftPlacement: '@',
            tooltipRightPlacement: '@',
            scrollBy: '@',
            autoRecalculate: '@',
            api: '=?'
          },

          template: [
            '<div class="ui-tabs-scrollable" ng-class="{\'show-drop-down\': !hideDropDown}">',
              '<button type="button" ng-mousedown="scrollButtonDown(\'left\', $event)" ng-mouseup="scrollButtonUp()" ng-hide="hideButtons"' +
              ' ng-disabled="disableLeft" class="btn nav-button left-nav-button"' +
              ' tooltip-placement="{{tooltipLeftDirection}}" tooltip-html="tooltipLeftHtml"></button>',
              '<div class="spacer" ng-class="{\'hidden-buttons\': hideButtons}" ng-transclude></div>',
              '<button type="button" ng-mousedown="scrollButtonDown(\'right\', $event)" ng-mouseup="scrollButtonUp()" ng-hide="hideButtons"' +
              ' ng-disabled="disableRight" class="btn nav-button right-nav-button"' +
              ' tooltip-placement="{{tooltipRightDirection}}" tooltip-html="tooltipRightHtml"></button>',
              '<div class="btn-group" dropdown is-open="isDropDownOpen" ng-hide="hideDropDown">',
                '<button type="button" class="btn" dropdown-toggle></button>',
                '<ul class="dropdown-menu dropdown-menu-right" role="menu" aria-labelledby="single-button">',
                  '<li role="menuitem" ng-repeat="tab in dropdownTabs" ng-class="{\'disabled\': tab.disabled}" ng-click="activateTab(tab)">',
                    '<a href="#"><span class="dropDownTabActiveMark" ng-style="{\'visibility\': tab.active?\'visible\':\'hidden\'}"></span>{{tab.tabScrollTitle}}</a>',
                  '</li>',
                '</ul>',
              '</div>',
            '</div>'
          ].join(''),

          link: function($scope, $el) {

            $scope.dropdownTabs = [];
            $scope.isDropDownOpen = false;
            $scope.hideButtons = true;
            $scope.hideDropDown = true;
            $scope.tooltipRightHtml = '';
            $scope.tooltipLeftHtml = '';
            $scope.disableLeft = true;
            $scope.disableRight = true;
            $scope.tooltipLeftDirection = $scope.tooltipLeftPlacement ? $scope.tooltipLeftPlacement : scrollableTabsetConfig.tooltipLeftPlacement;
            $scope.tooltipRightDirection =  $scope.tooltipRightPlacement ? $scope.tooltipRightPlacement : scrollableTabsetConfig.tooltipRightPlacement;

            $scope.api = {
              doRecalculate: function(){
                $timeout(function(){$scope.reCalcAll()});
              },

              scrollTabIntoView: function(arg){
                $timeout(function(){$scope.scrollTabIntoView(arg)});
              }
            };

            var mouseDownInterval = null;
            var isHolding = false;
            var winResizeTimeout

            var showDropDown = $scope.showDropDown ? $scope.showDropDown === 'true' : scrollableTabsetConfig.showDropDown;
            var showTooltips = $scope.showTooltips ? $scope.showTooltips === 'true' : scrollableTabsetConfig.showTooltips == true;
            var scrollByPixels = parseInt($scope.scrollBy ? $scope.scrollBy : scrollableTabsetConfig.scrollBy);

            $scope.onWindowResize = function() {
              // delay for a bit to avoid running lots of times.
              clearTimeout(winResizeTimeout);
              winResizeTimeout = setTimeout(function(){
                $scope.reCalcAll();
                $scope.$apply();
              }, 250);
            };

            var cancelMouseDownInterval = function() {
              isHolding = false;

              if(mouseDownInterval) {
                $interval.cancel(mouseDownInterval);
                mouseDownInterval = null;
              }
            };

            $scope.scrollButtonDown = function(direction, event) {
              event.stopPropagation();
              isHolding = true;

              var realScroll = direction === 'left' ? 0 - scrollByPixels : scrollByPixels;
              $scope.tabContainer.scrollLeft += realScroll;
              $scope.reCalcSides();

              mouseDownInterval = $interval(function() {

                if(isHolding) {
                  $scope.tabContainer.scrollLeft += realScroll;
                  $scope.reCalcSides();

                  if(event.target.disabled) {
                    cancelMouseDownInterval();
                  }
                }
              }, 100);
            }

            $scope.scrollButtonUp = function() {
              cancelMouseDownInterval();
            }

            $scope.activateTab = function(tab) {
              if(tab.disabled)return;
              tab.active = true;
              $timeout(function () {
                $scope.scrollTabIntoView();
              });
            }

            $scope.reCalcSides = function() {
              if(!$scope.tabContainer || $scope.hideButtons)return;
              $scope.disableRight = $scope.tabContainer.scrollLeft >= $scope.tabContainer.scrollWidth - $scope.tabContainer.offsetWidth;
              $scope.disableLeft = $scope.tabContainer.scrollLeft <= 0;

              if(showTooltips){
                $scope.reCalcTooltips();
              }
            };

            $scope.reCalcTooltips = function(){
              if(!$scope.tabContainer || $scope.hideButtons)return;
              var rightTooltips = [];
              var leftTooltips = [];

              var allTabs = $scope.tabContainer.querySelectorAll('li');
              angular.forEach(allTabs, function(tab) {

                var rightPosition = parseInt(tab.getBoundingClientRect().left + tab.getBoundingClientRect().width - $scope.tabContainer.getBoundingClientRect().left);
                var leftPosition = tab.getBoundingClientRect().left - $scope.tabContainer.getBoundingClientRect().left;
                var heading = tab.getAttribute("data-tabScrollHeading");
                var ignore = tab.getAttribute("data-tabScrollIgnore");

                if(rightPosition > $scope.tabContainer.offsetWidth && !ignore ) {
                  if(heading) {
                    rightTooltips.push(heading)
                  } else if (tab.textContent)rightTooltips.push(tab.textContent);
                }

                if (leftPosition < 0 && !ignore ) {
                  if(heading) {
                    leftTooltips.push(heading)
                  } else if (tab.textContent)leftTooltips.push(tab.textContent);
                }

              });

              var rightTooltipsHtml = rightTooltips.join('<br>');
              $scope.tooltipRightHtml = $sce.trustAsHtml(rightTooltipsHtml);

              var leftTooltipsHtml = leftTooltips.join('<br>');
              $scope.tooltipLeftHtml = $sce.trustAsHtml(leftTooltipsHtml);
            };

            $scope.scrollTabIntoView = function(arg){
              if(!$scope.tabContainer || $scope.hideButtons)return;

              var argInt = parseInt(arg);

              if(argInt) { // scroll tab index into view
                var allTabs = $scope.tabContainer.querySelectorAll('li');
                if(allTabs.length > argInt) { // only if its really exist
                  allTabs[argInt].scrollIntoView();
                }
              }

              else { // scroll selected tab into view
                var activeTab = $scope.tabContainer.querySelector('li.active');
                if(activeTab) {
                  activeTab.scrollIntoView();
                }
              }

              $scope.reCalcSides();
            };

            // init is called only once!
            $scope.init = function() {
              $scope.tabContainer = $el[0].querySelector('.spacer ul.nav-tabs');
              if(!$scope.tabContainer)return;

              var autoRecalc = $scope.autoRecalculate ? $scope.autoRecalculate === 'true' : scrollableTabsetConfig.autoRecalculate;
              if(autoRecalc) {
                var tabsetElement = angular.element($el[0].querySelector('.spacer div'));
                $scope.$watchCollection(
                    function () {
                      return tabsetElement.isolateScope() ? tabsetElement.isolateScope().tabs : false;
                    },
                    function () {
                      $timeout(function () {
                        $scope.reCalcAll()
                      });
                    }
                );
              }

              $scope.reCalcAll();

              // attaching event to window resize.
              angular.element($window).on('resize', $scope.onWindowResize);
            };

            // re-calculate if the scroll buttons are needed, than call re-calculate for both buttons.
            $scope.reCalcAll = function() {
              if(!$scope.tabContainer)return;

              $scope.hideButtons = $scope.tabContainer.scrollWidth <= $scope.tabContainer.offsetWidth;
              $scope.hideDropDown = showDropDown ? $scope.hideButtons : true;

              if(!$scope.hideButtons) {

                if(!$scope.hideDropDown) {
                  var allTabs = $scope.tabContainer.querySelectorAll('li');
                  $scope.dropdownTabs = [];
                  angular.forEach(allTabs, function (tab) {
                    var ignore = tab.getAttribute("data-tabScrollIgnore");
                    if(!ignore){
                      var heading = tab.getAttribute("data-tabScrollHeading");
                      var tabScope = angular.element(tab).isolateScope();
                      //push new field to use as title in the drop down.
                      tabScope.tabScrollTitle = heading ? heading : tabScope.headingElement.textContent;
                      $scope.dropdownTabs.push(tabScope);
                    }
                  });
                }

                $scope.reCalcSides();
              }
            };

            // this is how we init for the first time.
            $timeout(function(){
              $scope.init();
            });

            // when scope destroyed
            $scope.$on('$destroy', function () {
              angular.element($window).off('resize', $scope.onWindowResize);
            });

          }
        };
      }]);

