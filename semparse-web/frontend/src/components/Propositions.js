import PropTypes from 'prop-types';
import React from 'react';
import {
  Divider,
  Tab,
} from 'semantic-ui-react';
import RoleLabels from './RoleLabels';
import Semantics from './Semantics';

const Proposition = ({
  sense, spans, events, showVerbNet, showPropBank,
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
    { events && <Semantics events={events} /> }
  </div>
);

const propositionTypes = {
  sense: PropTypes.string.isRequired,
  showVerbNet: PropTypes.bool.isRequired,
  showPropBank: PropTypes.bool.isRequired,
  spans: PropTypes.arrayOf(PropTypes.object).isRequired,
  events: PropTypes.arrayOf(PropTypes.object).isRequired,
};

Proposition.propTypes = propositionTypes;

const Propositions = ({
  propositions, propIndex, handleTabChange, showPropBank, showVerbNet, showSemantics,
}) => {
  const propPanes = propositions.map((prop) => {
    const {
      sense, spans, events,
    } = prop;
    return {
      menuItem: sense,
      render: () => (
        <Tab.Pane key={sense} attached={false}>
          <Proposition
            showPropBank={showPropBank}
            showVerbNet={showVerbNet}
            spans={spans}
            events={showSemantics && events}
            sense={sense}
          />
        </Tab.Pane>
      ),
    };
  });

  return (
    <Tab
      menu={{ secondary: true, pointing: true }}
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
