(function() {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.dataset.service:DatasetListService
     * @description Dataset grid service. This service holds the dataset list like a cache and consume DatasetRestService to access to the REST api
     * @requires data-prep.services.dataset.service:DatasetRestService
     */
    function DatasetListService($q, DatasetRestService) {
        var self = this;
        var datasetsPromise;

        /**
         * @ngdoc property
         * @name datasets
         * @propertyOf data-prep.services.dataset.service:DatasetListService
         * @description the dataset list
         */
        self.datasets = null;

        /**
         * @ngdoc method
         * @name refreshDatasets
         * @methodOf data-prep.services.dataset.service:DatasetListService
         * @description Refresh datasets if no refresh is pending
         * @returns {promise} - the pending GET promise
         */
        self.refreshDatasets = function() {
            if(! datasetsPromise) {
                datasetsPromise = DatasetRestService.getDatasets()
                    .then(function(res) {
                        self.datasets = res.data;
                        datasetsPromise = null;
                        return self.datasets;
                    });
            }

            return datasetsPromise;
        };

        /**
         * @ngdoc method
         * @name getDatasetsPromise
         * @methodOf data-prep.services.dataset.service:DatasetListService
         * @description Return a promise that resolves the datasets list
         * @returns {promise} - the pending GET or resolved promise
         */
        self.getDatasetsPromise = function() {
            if(self.datasets === null) {
                return self.refreshDatasets();
            }
            else {
                return $q.when(self.datasets);
            }
        };

        /**
         * @ngdoc method
         * @name create
         * @methodOf data-prep.services.dataset.service:DatasetListService
         * @param {object} dataset The dataset to delete
         * @description Create a dataset from backend and refresh its internal list
         * @returns {promise} The pending POST promise
         */
        self.create = function(dataset) {
            var creationPromise = DatasetRestService.create(dataset);
            creationPromise.then(function() {
                self.refreshDatasets();
            });
            return creationPromise;
        };

        /**
         * @ngdoc method
         * @name update
         * @methodOf data-prep.services.dataset.service:DatasetListService
         * @param {object} dataset The dataset to delete
         * @description Update a dataset from backend and refresh its internal list
         * @returns {promise} The pending POST promise
         */
        self.update = function(dataset) {
            var updatePromise = DatasetRestService.update(dataset);
            updatePromise.then(function() {
                self.refreshDatasets();
            });
            return updatePromise;
        };

        /**
         * @ngdoc method
         * @name delete
         * @methodOf data-prep.services.dataset.service:DatasetListService
         * @param {object} dataset The dataset to delete
         * @description Delete a dataset from backend and from its internal list
         * @returns {promise} The pending DELETE promise
         */
        self.delete = function(dataset) {
            return DatasetRestService.delete(dataset)
                .then(function() {
                    var index = self.datasets.indexOf(dataset);
                    self.datasets.splice(index, 1);
                });
        };

        /**
         * @ngdoc method
         * @name getContent
         * @name data-prep.services.dataset.service:DatasetListService
         * @param {string} datasetId The dataset id
         * @param {boolean} metadata If false, the metadata will not be returned
         * @description Get a dataset content
         * @returns {promise} The pending GET promise
         */
        self.getContent = DatasetRestService.getContent;
    }

    angular.module('data-prep.services.dataset')
        .service('DatasetListService', DatasetListService);
})();