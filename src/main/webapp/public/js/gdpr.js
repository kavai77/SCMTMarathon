var app=angular.module('app', ['ngResource']);

app.controller('ctrl', function ($scope, $resource) {
    var res = initResources($scope, $resource);

    $scope.subscribed = false;


    $scope.subscribe = function () {
        $scope.errorMessages = new Array();

        if (!validateEmail($scope.emailText)) {
            $scope.errorMessages.push("Nem helyes az email cím!");
        }

        var recaptcha = document.getElementById('g-recaptcha-response').value;
        if (!recaptcha) {
            $scope.errorMessages.push("Bizonyítsd be, hogy nem vagy robot!");
        }

        if ($scope.errorMessages.length == 0) {
            $('#loadingDialog').modal('show');
            var sendData = new Object();
            sendData.email = $scope.emailText;
            sendData.recaptcha = recaptcha;

            res.NevezesService.subscribe(sendData, function() {
                $('#loadingDialog').modal('hide');
                $scope.subscribed = true;
            });
        }
    };
});

function validateEmail(email) {
  var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
  return re.test(email);
}
