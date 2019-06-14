import PropTypes from 'prop-types';
import React from 'react';
import {
  Divider,
  Tab,
} from 'semantic-ui-react';
import RoleLabels from './RoleLabels';
import Semantics from './Semantics';

const Proposition = ({
  sense, spans, mainEvent, events, showVerbNet, showPropBank,
}) => (
  <div>
    { spans && (
    <RoleLabels
      showVerbNet={showVerbNet}
      showPropBank={showPropBank}
      roles={spans}
      sense={sense}
    />
    ) }
    { spans && <Divider hidden /> }
    { events && <Semantics events={events} mainEvent={mainEvent} /> }
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
};

const Propositions = ({
  propositions, propIndex, handleTabChange, showPropBank, showVerbNet, showSemantics,
}) => {
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
      menu={{ color: 'blue', stackable: true, secondary: true, pointing: true }}
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
  showSemantics: PropTypes.bool.isRequired,
  handleTabChange: PropTypes.func.isRequired,
};


export default Propositions;
