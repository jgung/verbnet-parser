import PropTypes from 'prop-types';
import React from 'react';
import {
  Tab,
} from 'semantic-ui-react';
import RoleLabels from './RoleLabels';
import Semantics from './Semantics';

const Proposition = ({
  sense, spans, mainEvent, events, showVerbNet, showPropBank, showModifiers, functionalSemantics,
}) => (
  <div>
    { spans && (
    <RoleLabels
      showVerbNet={showVerbNet}
      showPropBank={showPropBank}
      showModifiers={showModifiers}
      roles={spans}
      sense={sense}
    />
    ) }
    { events && (
    <Semantics
      functionalView={functionalSemantics}
      events={events}
      mainEvent={mainEvent}
    />
    ) }
  </div>
);

const propositionTypes = {
  sense: PropTypes.string.isRequired,
  spans: PropTypes.arrayOf(PropTypes.object).isRequired,
  events: PropTypes.arrayOf(PropTypes.object).isRequired,
};

Proposition.propTypes = {
  ...propositionTypes,
  showVerbNet: PropTypes.bool.isRequired,
  showPropBank: PropTypes.bool.isRequired,
  functionalSemantics: PropTypes.bool.isRequired,
};

const Propositions = ({
  propositions, propIndex, handleTabChange, showPropBank, showVerbNet, showModifiers, showSemantics,
  functionalSemantics, showTabs = false,
}) => {
  if (showTabs) {
    if (propositions.length >= propIndex) {
      const {
        sense, spans, events, mainEvent,
      } = propositions[propIndex];
      return (
        <Proposition
          showPropBank={showPropBank}
          showVerbNet={showVerbNet}
          showModifiers={showModifiers}
          spans={spans}
          events={showSemantics && events}
          sense={sense}
          mainEvent={mainEvent}
          functionalSemantics={functionalSemantics}
        />
      );
    }
    return '';
  }

  const propPanes = propositions.map((prop, index) => {
    const {
      sense, spans, events, mainEvent,
    } = prop;
    return {
      menuItem: sense + (' '.repeat(index)),
      render: () => (
        <Proposition
          showPropBank={showPropBank}
          showVerbNet={showVerbNet}
          showModifiers={showModifiers}
          functionalSemantics={functionalSemantics}
          spans={spans}
          events={showSemantics && events}
          sense={sense}
          mainEvent={mainEvent}
        />
      ),
    };
  });

  return (
    <Tab
      menu={{
        color: 'blue', stackable: true, secondary: true, pointing: true,
      }}
      activeIndex={propIndex}
      onTabChange={handleTabChange}
      panes={propPanes}
    />
  );
};

Propositions.propTypes = {
  propositions: PropTypes.arrayOf(PropTypes.shape(propositionTypes)).isRequired,
  propIndex: PropTypes.number.isRequired,
  showPropBank: PropTypes.bool.isRequired,
  showVerbNet: PropTypes.bool.isRequired,
  showModifiers: PropTypes.bool.isRequired,
  showSemantics: PropTypes.bool.isRequired,
  handleTabChange: PropTypes.func.isRequired,
  functionalSemantics: PropTypes.bool,
  showTabs: PropTypes.bool,
};

Propositions.defaultProps = {
  showTabs: false,
  functionalSemantics: false,
};

export default Propositions;
