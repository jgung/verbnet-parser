import { connect } from 'react-redux';
import { getDemoState } from '../selectors';
import * as actions from '../actions';
import Labels from '../../components/Labels';

const mapStateToProps = (state) => {
  const demoState = getDemoState(state);
  const { tokens, props } = demoState.message;
  return {
    propIndex: Math.min(demoState.activeIndex, props.length - 1),
    tokens,
  };
};

const mapDispatchToProps = dispatch => (
  {
    setTabIndex: (activeIndex) => {
      dispatch(actions.setTabIndex(activeIndex));
    },
  }
);

export default connect(mapStateToProps, mapDispatchToProps)(Labels);
