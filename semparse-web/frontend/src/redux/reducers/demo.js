import t from '../actions';

const initialState = {
  utterance: '',
  message: {
    props: [],
    tokens: [],
  },
  success: false,
  failure: false,
  loading: false,
  errorMessage: '',

  activeIndex: 0,
  showOptions: false,
  showPropBank: true,
  showVerbNet: true,
  showModifiers: true,
  showSemantics: true,
  functionalSemantics: true,
};

export default function (state = initialState, action) {
  switch (action.type) {
    case t.SET_UTTERANCE:
      return { ...state, utterance: action.payload };
    case t.SET_TAB_INDEX:
      return { ...state, activeIndex: action.payload };
    case t.TOGGLE_VIEW_OPTIONS:
      return { ...state, showOptions: !state.showOptions };
    case t.TOGGLE_PROPBANK:
      return { ...state, showPropBank: !state.showPropBank };
    case t.TOGGLE_VERBNET:
      return { ...state, showVerbNet: !state.showVerbNet };
    case t.TOGGLE_MODIFIERS:
      return { ...state, showModifiers: !state.showModifiers };
    case t.TOGGLE_SEMANTICS:
      return { ...state, showSemantics: !state.showSemantics };
    case t.TOGGLE_INDEX_VIEW:
      return { ...state, functionalSemantics: !state.functionalSemantics };

    case t.SUBMIT_UTTERANCE_REQUEST:
      return {
        ...state, success: false, failure: false, loading: true,
      };
    case t.SUBMIT_UTTERANCE_SUCCESS:
      return {
        ...state,
        success: true,
        failure: false,
        loading: false,
        tabIndex: 0,
        message: action.payload,
      };
    case t.SUBMIT_UTTERANCE_FAILURE:
      return {
        ...state,
        success: false,
        failure: true,
        loading: false,
        message: initialState.message,
        errorMessage: action.payload,
      };
    default:
      return state;
  }
}
