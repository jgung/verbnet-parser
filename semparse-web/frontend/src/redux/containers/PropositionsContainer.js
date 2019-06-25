import { connect } from 'react-redux';
import { getDemoState } from '../selectors';
import * as actions from '../actions';
import Propositions from '../../components/Propositions';

const mapStateToProps = (state) => {
  const demoState = getDemoState(state);
  const { props } = demoState.message;
  return {
    propIndex: Math.min(demoState.activeIndex, props.length - 1),
    showPropBank: demoState.showPropBank,
    showVerbNet: demoState.showVerbNet,
    showModifiers: demoState.showModifiers,
    showSemantics: demoState.showSemantics,
    functionalSemantics: demoState.functionalSemantics,
    propositions: props,
  };
};

const mapDispatchToProps = dispatch => (
  {
    handleTabChange: (e, { activeIndex }) => {
      dispatch(actions.setTabIndex(activeIndex));
    },
  }
);

export default connect(mapStateToProps, mapDispatchToProps)(Propositions);
