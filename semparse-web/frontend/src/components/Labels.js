import PropTypes from 'prop-types';
import React from 'react';
import { Label, Message } from 'semantic-ui-react';

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

export default Labels;
