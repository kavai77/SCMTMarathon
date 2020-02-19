var app=angular.module('app', ['ngResource']);

app.controller('ctrl', function ($scope, $resource) {
    $scope.isActive = function (viewLocation) {
        return viewLocation === window.location.pathname;
    };

    var res = initResources($scope, $resource);

    $scope.nevezes = res.NevezesService.get();
    $scope.nevezett = false;
    $scope.hirlevel = false;

    $scope.nevezesDone = function () {
        $scope.errorMessages = new Array();
        $scope.tavClass=null;
        $scope.nevClass=null;
        $scope.nemClass=null;
        $scope.evClass=null;
        $scope.emailClass=null;
        $scope.licenszClass=null;
        $scope.poloClass=null;
        $scope.checkClass=null;
        if (isBlank($scope.tav)) {
            $scope.tavClass='has-error';
            $scope.errorMessages.push("A táv kitöltése kötelező!");
        }
        if (isBlank($scope.nev)) {
            $scope.nevClass='has-error';
            $scope.errorMessages.push("A név kitöltése kötelező!");
        }
        if (!$scope.nem) {
            $scope.nemClass='has-error';
            $scope.errorMessages.push("A nem kitöltése kötelező!");
        }
        var ev = parseInt($scope.ev);
        if (isNaN(ev) || ev < 1900 || ev > new Date().getFullYear()) {
            $scope.evClass='has-error';
            $scope.errorMessages.push("A születési év kitöltése kötelező!");
        }
        if (isBlank($scope.email)) {
            $scope.emailClass='has-error';
            $scope.errorMessages.push("Az email cím kitöltése kötelező!");
        }
        if ($scope.nevezes.triatlonLicensz && isBlank($scope.licenszSzam)) {
            $scope.licenszClass='has-error';
            $scope.errorMessages.push("A Magyar Triatlon Szövetség által kiadott licensz megadása kötelező!");
        }
        if (isBlank($scope.poloMeret)) {
            $scope.poloClass='has-error';
            $scope.errorMessages.push("Válaszd ki a pólómértedet!");
        }
        if (!$scope.check) {
            $scope.checkClass='has-error';
            $scope.errorMessages.push("A versenyszabályzat elolvasása kötelező!");
        }
        var recaptcha = document.getElementById('g-recaptcha-response').value;
        if (!recaptcha) {
            $scope.errorMessages.push("Bizonyítsd be, hogy nem vagy robot!");
        }
        if ($scope.errorMessages.length == 0) {
            $('#loadingDialog').modal('show');
            var sendData = new Object();
            sendData.tav = $scope.tav;
            sendData.nev = $scope.nev;
            sendData.nem = $scope.nem;
            sendData.ev = ev;
            sendData.egyesulet = emptyForBlank($scope.egyesulet);
            sendData.email = $scope.email;
            sendData.poloMeret = $scope.poloMeret;
            sendData.licenszSzam = $scope.licenszSzam;
            sendData.recaptcha = recaptcha;
            sendData.hirlevel = $scope.hirlevel;
            res.NevezesService.store(sendData, function() {
                $('#loadingDialog').modal('hide');
                $scope.nevezett = true;
            }, function(response) {
                $('#loadingDialog').modal('hide');
            });
        }
    };
});

function isBlank(str) {
    return (!str || /^\s*$/.test(str));
}

function emptyForBlank(data) {
    return !isBlank(data) ? data : "";
}
