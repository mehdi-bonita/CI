var module;
try {
  module = angular.module('bonitasoft.ui.fragments');
} catch (e) {
  module = angular.module('bonitasoft.ui.fragments', []);
  angular.module('bonitasoft.ui').requires.push('bonitasoft.ui.fragments');
}
module.directive('pbFragmentGestionDocFragment', function() {
  return {
    template: '<div>    <div class="row">\n        <div pb-property-values=\'bedc2386-ef8f-4fd7-a6ce-ea6815f31162\'>\n    <div class="col-xs-12  col-sm-12  col-md-12  col-lg-12" ng-class="properties.cssClasses"\n         ng-if="!properties.hidden" ng-repeat="$item in ($collection = properties.repeatedCollection) track by $index">\n            <div class="row">\n        <div pb-property-values=\'27e3da39-70cf-46b7-b8f7-0c4b6de768ad\'>\n    <div ng-if="!properties.hidden" class="component col-xs-12  col-sm-12  col-md-6  col-lg-6" ng-class="properties.cssClasses">\n        <pb-upload></pb-upload>\n    </div>\n</div><div pb-property-values=\'f7279d6a-6726-43f1-8061-d8ac415bed9a\'>\n    <div ng-if="!properties.hidden" class="component col-xs-12  col-sm-12  col-md-6  col-lg-6" ng-class="properties.cssClasses">\n        <pb-button></pb-button>\n    </div>\n</div>\n    </div>\n\n    </div>\n</div>\n\n\n    </div>\n    <div class="row">\n        <div pb-property-values=\'ae87e7cb-b902-492e-aabb-60fb8840ee81\'>\n    <div ng-if="!properties.hidden" class="component col-xs-12  col-sm-12  col-md-12  col-lg-12" ng-class="properties.cssClasses">\n        <pb-button></pb-button>\n    </div>\n</div>\n    </div>\n</div>'
  };
});
