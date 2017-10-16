/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Bozo the clown directive', function () {
    'use strict';

    var scope;
    var createElement;
    var element;

    beforeEach(angular.mock.module('data-prep.easter-eggs'));

    beforeEach(inject(function ($rootScope, $compile) {
        scope = $rootScope.$new();
        createElement = function () {
            element = angular.element('<bozo-the-clown></bozo-the-clown>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };
    }));

    it('should render bozo the clown div', function () {
        //when
        createElement();

        //then
        expect(element.find('#bozo').attr('src')).toBe('assets/images/bozo-the-clown/bozo-the-clown.gif');
    });
});
