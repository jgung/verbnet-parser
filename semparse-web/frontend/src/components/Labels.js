import PropTypes from 'prop-types';
import React from 'react';
import { Label, Message } from 'semantic-ui-react';
import { connect } from 'react-redux';
import { getDemoState } from '../redux/selectors';
import * as actions from '../redux/actions';

const Labels = ({ propIndex, tokens, setTabIndex }) => {
  const sentence = tokens.map((token) => {
    const {
      text, label, isPredicate, index,
    } = token;
    if (isPredicate) {
      return (
        <Label
          key={index}
          as="a"
          size="medium"
          color="blue"
          basic={index !== propIndex}
          content={text}
          detail={label}
          onClick={() => setTabIndex(index)}
        />
      );
    }
    return ` ${text} `;
  });
  return (
    <Message style={{ flexDirection: 'row' }}>
      {sentence}
    </Message>
  );
};

Labels.propTypes = {
  propIndex: PropTypes.number.isRequired,
  tokens: PropTypes.arrayOf(PropTypes.object).isRequired,
  setTabIndex: PropTypes.func.isRequired,
};

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
