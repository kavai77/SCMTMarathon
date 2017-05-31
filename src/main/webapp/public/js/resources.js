function initResources($scope, $resource) {
    var defaultServerError = function () {
        $scope.errorMessage = 'Oops! Something went wrong :-(';
    };

    var urlEncoded = 'application/x-www-form-urlencoded';
    var urlEncodedTransform = function(data) {
        var str = [];
        for(var p in data)
            str.push(encodeURIComponent(p) + "=" + encodeURIComponent(data[p]));
        return str.join("&");
    };

    var resource = new Object();

    resource.NevezesService = $resource('/public/nevezes/:action', {}, {
        get: {params: {action: 'get'}, interceptor: {responseError : defaultServerError}},
        store: {method: 'POST', params: {action: 'store'}, headers: {'Content-Type': urlEncoded}, transformRequest: urlEncodedTransform, interceptor: {responseError : defaultServerError}}
    });

    return resource;

}