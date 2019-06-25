import PropTypes from 'prop-types';
import React from 'react';
import {
  Label,
  List,
} from 'semantic-ui-react';

const MAX_LENGTH = 50;

const spanType = PropTypes.shape({
  label: PropTypes.string,
  text: PropTypes.string,
  vn: PropTypes.string,
  pb: PropTypes.string,
  description: PropTypes.string,
  isPredicate: PropTypes.bool,
  isModifier: PropTypes.bool,
});

// credit: https://stackoverflow.com/a/5723274
function truncate(fullStr, strLen, separator) {
  const len = strLen || MAX_LENGTH;
  if (fullStr.length <= len) return fullStr;

  const sep = separator || '...';

  const sepLen = sep.length;
  const charsToShow = len - sepLen;
  const frontChars = Math.ceil(charsToShow / 2);
  const backChars = Math.floor(charsToShow / 2);

  return fullStr.substr(0, frontChars)
           + sep
           + fullStr.substr(fullStr.length - backChars);
}

const Span = ({
  sense, span, showVerbNet, showPropBank, showModifiers,
}) => {
  const {
    vn, pb, description, text, isPredicate, isModifier,
  } = span;
  return (
    <Label basic>
      <Label
        color="black"
        basic
        size="large"
        content={truncate(text)}
      />
      {
        isPredicate
          && (
          <Label
            color="blue"
            size="large"
            content={sense}
          />
          )
      }
      {
        showVerbNet && vn && !isPredicate
          && (
            <Label
              color="blue"
              basic
              size="large"
              content={vn}
            />
          )
      }
      {
        isModifier && showModifiers && pb && (
        <Label
          color="grey"
          basic
          size="large"
          content={pb}
          detail={description || undefined}
        />
        )
      }
      {
        showPropBank && !isModifier && pb && !isPredicate
          && (
            <Label
              color="purple"
              basic
              size="large"
              content={pb}
              detail={description || undefined}
            />
          )
      }
    </Label>
  );
};

Span.propTypes = {
  sense: PropTypes.string.isRequired,
  span: spanType.isRequired,
  showVerbNet: PropTypes.bool.isRequired,
  showPropBank: PropTypes.bool.isRequired,
  showModifiers: PropTypes.bool.isRequired,
};

const RoleLabels = ({
  sense, roles, showVerbNet, showPropBank, showModifiers,
}) => {
  const spans = roles.map((span) => {
    const { start } = span;
    return (
      <List.Item key={start}>
        <Span showVerbNet={showVerbNet} showPropBank={showPropBank} showModifiers={showModifiers} sense={sense} span={span} color="black" />
      </List.Item>
    );
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
  showVerbNet: PropTypes.bool.isRequired,
  showPropBank: PropTypes.bool.isRequired,
  showModifiers: PropTypes.bool.isRequired,
};

export default RoleLabels;
