'use strict';

var log = log4javascript.getLogger("blueprintController-logger");

angular.module('uluwatuControllers').controller('blueprintController', ['$scope', '$rootScope', 'AccountBlueprint', 'GlobalBlueprint',
    function ($scope, $rootScope, AccountBlueprint, GlobalBlueprint) {
        $rootScope.blueprints = AccountBlueprint.query();
        initializeBlueprint();

        $scope.createBlueprint = function () {
            AccountBlueprint.save($scope.blueprint, function (result) {
                GlobalBlueprint.get({ id: result.id}, function(success) {
                  $rootScope.blueprints.push(success);
                  initializeBlueprint();
                  $scope.modifyStatusMessage($rootScope.error_msg.blueprint_success1 + success.id + $rootScope.error_msg.blueprint_success2);
                  $scope.modifyStatusClass("has-success");
                  $scope.blueprintForm.$setPristine();
                  angular.element(document.querySelector('#panel-create-blueprints-collapse-btn')).click();
                });
            }, function (error) {
                $scope.modifyStatusMessage($rootScope.error_msg.blueprint_template_failed + ": " + error.data.message);
                $scope.modifyStatusClass("has-error");
            });
        }

        $scope.deleteBlueprint = function (blueprint) {
            GlobalBlueprint.delete({ id: blueprint.id }, function (success) {
                $rootScope.blueprints.splice($rootScope.blueprints.indexOf(blueprint), 1);
                $scope.modifyStatusMessage($rootScope.error_msg.blueprint_delete_success1 + blueprint.id + $rootScope.error_msg.blueprint_delete_success2);
                $scope.modifyStatusClass("has-success");
            }, function (error) {
                $scope.modifyStatusMessage($rootScope.error_msg.blueprint_delete_failed  + ": " + error.data.message);
                $scope.modifyStatusClass("has-error");
            });
        }

        function initializeBlueprint() {
            $scope.blueprint = {}
        }
    }
]);
