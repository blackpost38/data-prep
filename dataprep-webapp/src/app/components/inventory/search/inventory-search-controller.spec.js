/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Inventory Search controller', function () {
    'use strict';

    var component, scope;

    beforeEach(angular.mock.module('data-prep.inventory-search'));

    beforeEach(inject(function($rootScope, $componentController) {
        scope = $rootScope.$new();
        component = $componentController('inventorySearch', {$scope: scope});
    }));

    describe('search ', function() {
        beforeEach(inject(function ($q, InventoryService) {
            spyOn(InventoryService, 'search').and.returnValue($q.when({}));
        }));

        it('should call the easter eggs service', inject(function (InventoryService) {
            //when
            component.searchInput = 'barcelona';
            component.search();
            scope.$digest();

            //then
            expect(InventoryService.search).toHaveBeenCalledWith('barcelona');
            expect(component.results).toEqual({});
        }));
    });
});