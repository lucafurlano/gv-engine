angular.module('gvconsole')
.service('FlowService', ['ENDPOINTS', '$http', function(Endpoints, $http){

	this.executeFlow = function(serviceName, operationName, properties, payload, auth) {

	      var request = {
	        method: 'GET',
	        url : Endpoints.gvesb + '/' + serviceName + '/' + operationName,
	        transformResponse: [function (data) {
	          return data;
	        }]
	      };

	      if (auth) {
	    	  request.headers = {
		        	'authorization' : auth
		        }
	      } else {
	    	  request.headers = {
			        	'authorization' : undefined
			        }
	      }

	      if (properties) {
	    	  request.params = properties;
	      }

	      if (payload) {
	    	  request.method = 'POST';
	    	  request.data = payload;
	      }

	     return $http(request);

	}

	this.getServices = function(){
		return $http.get(Endpoints.gvesb);
	}

	this.getConfigInfo = function() {
		return $http.get(Endpoints.gvconfig + '/deploy')
	}

}]);

angular.module('gvconsole')
.controller('FlowController', [ 'Base64', 'FlowService', '$scope', function(Base64, FlowService, $scope){


    $scope.operations = [];
    $scope.service = {};
		var instance = this;
		this.configInfo = {};

	  this.loadConfigInfo = function() {

			FlowService.getConfigInfo().then(
					function(response){
						instance.configInfo = response.data;
					},
					function(response){
						switch (response.status) {

								case 401: case 403:
									$location.path('login');
									break;

								default:
									instance.alerts.push({type: 'danger', msg: 'Data not available'});
									break;
						}
			});

		}

    FlowService.getServices().then(
    function(response) {
      angular.forEach(response.data, function(service, sName) {
        angular.forEach(service.operations, function(operation, oName) {
          $scope.operations.push({ service: sName, operation: oName, key: sName+'/'+oName});
        });
      });
    });

    $scope.addParameter = function(){

  		if ($scope.service.properties == undefined) {
  			$scope.service.properties = {};
  		}

  		if ($scope.key && $scope.key.trim().length > 0) {
  			   $scope.service.properties[$scope.key]= $scope.value;
  			   $scope.key = '';
  			   $scope.value = '';
  		}
  	};

    $scope.removeParameter=function(key){
  		 delete $scope.service.properties[key];
  	};

  	$scope.flag = true;

    $scope.run = function(){

    	$scope.flag = false;

	      var auth;
	      if($scope.username && $scope.password){
	        auth = 'Basic ' +  Base64.encode($scope.username + ':' + $scope.password);
	      }


	      FlowService.executeFlow($scope.flow.service, $scope.flow.operation, $scope.service.properties, $scope.service.object, auth)
	                 .then(function(response){

				    	  $scope.output = response.data;

				    	  $scope.flag = true;

				      }, function(response){

				    	  $scope.output = response.data;

				    	  $scope.flag = true;

				      });
    }


    angular.element("textarea").keydown(function(e) {
        if(e.keyCode === 9) { // tab was pressed
            // get caret position/selection
            var start = this.selectionStart;
            var end = this.selectionEnd;

            var $this = angular.element(this);
            var value = $this.val();

            // set textarea value to: text before caret + tab + text after caret
            $this.val(value.substring(0, start)
                        + "\t"
                        + value.substring(end));

            // put caret at right position again (add one for the tab)
            this.selectionStart = this.selectionEnd = start + 1;

            // prevent the focus lose
            e.preventDefault();
        }
    });


}]);
