var app = angular.module('app', ['ngMessages', 'ngRoute']);

// app.config(['$locationProvider', '$routeProvider', function ($locationProvider, $routeProvider) {
//     // hide index.html# in browser url
//     $locationProvider.html5Mode({
//         enabled: true
//     });
//
//     // $routeProvider.when('/result/:jobId', {
//     //     templateUrl: '/result/',
//     //     controller: 'ResultController'
//     // });
// }]);

app.service('JobService', ['$http', function ($http) {

    this.createJob = function createJob() {
        return $http({
            method: 'GET',
            url: 'rest/createJob',
            headers: 'Accept:application/json'
        }).then(function (response) {
            console.log("created job: " + response.data);
            return response.data;
        });
    };

    this.submitJob = function submitJob(job) {
        console.log("submitting job: " + job);
        return $http({
            method: 'POST',
            url: 'rest/submitJob',
            data: job,
            headers: 'Accept:application/json'
        });
    };

    this.getJob = function getJob(jobId) {
        console.log("getting job: " + jobId);
        return $http({
            url: 'rest/getJob/' + jobId,
            headers: 'Accept:application/json'
        });
    };

    // this.showResults = function showResults(job) {
    //     console.log("loading results for job " + job);
    //     console.log(job);
    //     return $http({
    //         method: 'GET',
    //         url: 'result/' + job.jobId
    //     });
    // };


}]);

app.controller('SubmitController', ['$scope', '$window', 'JobService', function ($scope, $window, JobService) {

    $scope.createJob = function () {
        JobService.createJob().then(function (data) {
            console.log("creating job");
            $scope.job = data;
            console.log(data)
        });
    };

    $scope.submitJob = function () {
        $scope.submitted = true;
        if ($scope.submitForm.$valid) {
            console.log($scope.job);
            JobService.submitJob($scope.job).then(function success(response) {
                    console.log(response);
                    console.log("job successfully submitted");
                    $window.location.href = '/result/' + response.data.jobId;
                    // $scope.errorMessage = '';
                    // $scope.getJob();
                    // $scope.user = null;
                    // $scope.submitted = false;
                },
                function error(response) {
                    if (response.status === 409) {
                        $scope.errorMessage = response.data.message;
                    }
                    else {
                        $scope.errorMessage = 'Error adding user!';
                    }
                    $scope.message = '';
                });
        }
    };
    $scope.createJob();
}]);

app.controller('ResultController', ['$scope', '$location', '$interval', 'JobService', function ($scope, $location, $interval, JobService) {
    var jobId = $location.path().split("/")[2];
    $scope.getJob = function () {
        console.info("splitted jobId " + jobId);
        JobService.getJob(jobId).then(function (response) {
            $scope.job = response.data;
            console.log("job is " + response.data.jobId);
        });
    };

    var updateStatus = function () {
        console.info("updating result page...");
        $scope.getJob();
    };
    $scope.getJob();
}]);
