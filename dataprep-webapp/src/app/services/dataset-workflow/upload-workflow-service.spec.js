/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('UploadWorkflow Service', () => {
    'use strict';

    beforeEach(angular.mock.module('data-prep.services.datasetWorkflowService'));
    beforeEach(inject(($state, $q, StateService, DatasetSheetPreviewService, MessageService, DatasetService) => {
        spyOn($state, 'go').and.returnValue();
        spyOn(StateService, 'setPreviousRoute').and.returnValue();
        spyOn(DatasetSheetPreviewService, 'loadPreview').and.returnValue($q.when());
        spyOn(DatasetSheetPreviewService, 'display').and.returnValue();
        spyOn(MessageService, 'error').and.returnValue();
        spyOn(DatasetService, 'refreshDatasets').and.returnValue();
    }));

    describe('open dataset', () => {
        beforeEach(inject(($state) => {
            spyOn($state, 'href').and.returnValue('absoluetUrl');
        }));

        it('should redirect to playground when dataset is not a draft', inject(($state, UploadWorkflowService) => {
            //given
            const dataset = { name: 'Customers (50 lines)', id: 'aA2bc348e933bc2' };

            //when
            UploadWorkflowService.openDataset(dataset);

            //then
            expect($state.go).toHaveBeenCalledWith('playground.dataset', { datasetid: dataset.id });
        }));

        it('should set back route before redirection when dataset is not a draft', inject((UploadWorkflowService, StateService) => {
            //given
            const dataset = { name: 'Customers (50 lines)', id: 'aA2bc348e933bc2' };
            //when
            UploadWorkflowService.openDataset(dataset);

            //then
            expect(StateService.setPreviousRoute).toHaveBeenCalledWith('nav.index.datasets');
        }));

        it('should open sheet preview when dataset is a draft', inject(($rootScope, UploadWorkflowService, DatasetSheetPreviewService) => {
            //given
            const dataset = {
                name: 'Customers (50 lines)',
                id: 'aA2bc348e933bc2',
                type: 'application/vnd.ms-excel',
                draft: true,
            };

            //when
            UploadWorkflowService.openDataset(dataset);
            $rootScope.$digest();

            //then
            expect(DatasetSheetPreviewService.loadPreview).toHaveBeenCalledWith(dataset, false, '');
            expect(DatasetSheetPreviewService.display).toHaveBeenCalled();
        }));

        it('should open a dataset in a new tab on scroll click', inject(($stateParams, $rootScope, $state, $window, StateService, UploadWorkflowService) => {
            //given
            const event = {
                which: 2,
            };
            spyOn($window, 'open').and.returnValue();
            const dataset = {
                id: 'de618c62ef97b3a95b5c171bc077ffe22e1d6f79',
            };

            //when
            UploadWorkflowService.openDataset(dataset, event);

            //then
            expect($state.go).not.toHaveBeenCalled();
            expect($state.href).toHaveBeenCalledWith('playground.dataset', { datasetid: dataset.id }, { absolute: true });
            expect($window.open).toHaveBeenCalledWith('absoluetUrl', '_blank');
        }));

        it('should open a preparation in a new tab on ctrl + click combination', inject(($stateParams, $rootScope, $state, $window, StateService, UploadWorkflowService) => {
            //given
            const event = {
                which: 1,
                ctrlKey: true,
            };
            spyOn($window, 'open').and.returnValue();
            const dataset = {
                id: 'de618c62ef97b3a95b5c171bc077ffe22e1d6f79',
            };

            //when
            UploadWorkflowService.openDataset(dataset, event);

            //then
            expect($state.go).not.toHaveBeenCalled();
            expect($state.href).toHaveBeenCalledWith('playground.dataset', { datasetid: dataset.id }, { absolute: true });
            expect($window.open).toHaveBeenCalledWith('absoluetUrl', '_blank');
        }));

        it('should open a preparation in a new tab on metakey + click combination', inject(($stateParams, $rootScope, $state, $window, StateService, UploadWorkflowService) => {
            //given
            const event = {
                which: 1,
                metaKey: true,
            };
            spyOn($window, 'open').and.returnValue();
            const dataset = {
                id: 'de618c62ef97b3a95b5c171bc077ffe22e1d6f79',
            };

            //when
            UploadWorkflowService.openDataset(dataset, event);

            //then
            expect($state.go).not.toHaveBeenCalled();
            expect($state.href).toHaveBeenCalledWith('playground.dataset', { datasetid: dataset.id }, { absolute: true });
            expect($window.open).toHaveBeenCalledWith('absoluetUrl', '_blank');
        }));
    });

    describe('draft', () => {
        it('should load excel preview and display it', inject(($rootScope, UploadWorkflowService, DatasetSheetPreviewService) => {
            //given
            const draft = { type: 'application/vnd.ms-excel' };

            //when
            UploadWorkflowService.openDraft(draft, true, 'test');
            $rootScope.$digest();

            //then
            expect(DatasetSheetPreviewService.loadPreview).toHaveBeenCalledWith(draft, true, 'test');
            expect(DatasetSheetPreviewService.display).toHaveBeenCalled();
        }));

        it('should display error message with unknown type', inject((DatasetSheetPreviewService, UploadWorkflowService, MessageService) => {
            //given
            const draft = { type: 'application/myCustomType' };

            //when
            UploadWorkflowService.openDraft(draft);

            //then
            expect(DatasetSheetPreviewService.loadPreview).not.toHaveBeenCalled();
            expect(DatasetSheetPreviewService.display).not.toHaveBeenCalled();
            expect(MessageService.error).toHaveBeenCalledWith('PREVIEW_NOT_IMPLEMENTED_FOR_TYPE_TITLE', 'PREVIEW_NOT_IMPLEMENTED_FOR_TYPE_TITLE');
        }));

        it('should refresh dataset list and display error when draft has no type yet', inject((UploadWorkflowService, DatasetSheetPreviewService, DatasetService, MessageService) => {
            //given
            const draft = {};

            //when
            UploadWorkflowService.openDraft(draft);

            //then
            expect(DatasetSheetPreviewService.loadPreview).not.toHaveBeenCalled();
            expect(DatasetSheetPreviewService.display).not.toHaveBeenCalled();
            expect(MessageService.error).toHaveBeenCalledWith('FILE_FORMAT_ANALYSIS_NOT_READY_TITLE', 'FILE_FORMAT_ANALYSIS_NOT_READY_CONTENT');
            expect(DatasetService.refreshDatasets).toHaveBeenCalled();
        }));
    });
});
