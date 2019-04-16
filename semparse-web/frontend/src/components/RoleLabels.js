import PropTypes from 'prop-types';
import React from 'react';
import {
  Label,
  List,
} from 'semantic-ui-react';

const spanType = PropTypes.shape({
  label: PropTypes.string,
  text: PropTypes.string,
  isPredicate: PropTypes.bool,
});

const Span = ({ sense, span, color = 'blue' }) => {
  const { label, text, isPredicate } = span;
  return (
    <List.Item>
      <Label
        color={isPredicate ? 'blue' : color}
        basic={!isPredicate}
        size="large"
        content={isPredicate ? sense : label}
        detail={text}
      />
    </List.Item>
  );
};

Span.propTypes = {
  sense: PropTypes.string.isRequired,
  span: spanType.isRequired,
  color: PropTypes.string.isRequired,
};

const RoleLabels = ({ sense, roles }) => {
  const spans = roles.map((span) => {
    const { start } = span;
    return (<Span sense={sense} key={start} span={span} color="black" />);
  });
  return (
    <List>
      {spans}
    </List>
  );
};

RoleLabels.propTypes = {
  sense: PropTypes.string.isRequired,
  roles: PropTypes.arrayOf(spanType).isRequired,
};

export default RoleLabels;
