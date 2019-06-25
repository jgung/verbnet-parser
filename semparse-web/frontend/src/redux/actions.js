import { createAction } from 'redux-actions';

const types = {
  SET_UTTERANCE: 'SET_UTTERANCE',
  SET_TAB_INDEX: 'SET_INDEX',

  TOGGLE_PROPBANK: 'TOGGLE_PROPBANK',
  TOGGLE_VERBNET: 'TOGGLE_VERBNET',
  TOGGLE_MODIFIERS: 'TOGGLE_MODIFIERS',
  TOGGLE_SEMANTICS: 'TOGGLE_SEMANTICS',
  TOGGLE_INDEX_VIEW: 'TOGGLE_INDEX_VIEW',
  TOGGLE_VIEW_OPTIONS: 'TOGGLE_SHOW_OPTIONS',

  SUBMIT_UTTERANCE_REQUEST: 'SUBMIT_UTTERANCE_REQUEST',
  SUBMIT_UTTERANCE_SUCCESS: 'SUBMIT_UTTERANCE_SUCCESS',
  SUBMIT_UTTERANCE_FAILURE: 'SUBMIT_UTTERANCE_FAILURE',
};

export const setUtterance = createAction(types.SET_UTTERANCE);
export const setTabIndex = createAction(types.SET_TAB_INDEX);

export const togglePropbank = createAction(types.TOGGLE_PROPBANK);
export const toggleVerbnet = createAction(types.TOGGLE_VERBNET);
export const toggleModifiers = createAction(types.TOGGLE_MODIFIERS);
export const toggleSemantics = createAction(types.TOGGLE_SEMANTICS);
export const toggleIndexView = createAction(types.TOGGLE_INDEX_VIEW);
export const toggleViewOptions = createAction(types.TOGGLE_VIEW_OPTIONS);

export const submitUtteranceRequest = createAction(types.SUBMIT_UTTERANCE_REQUEST);
export const submitUtteranceSuccess = createAction(types.SUBMIT_UTTERANCE_SUCCESS);
export const submitUtteranceFailure = createAction(types.SUBMIT_UTTERANCE_FAILURE);

export default types;
