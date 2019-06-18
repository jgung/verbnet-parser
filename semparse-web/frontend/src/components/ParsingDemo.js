import React, { Component } from 'react';
import {
  Accordion,
  Button, Checkbox,
  Form,
  Icon,
  Message,
  Segment,
} from 'semantic-ui-react';
import PropTypes from 'prop-types';
import PropositionsContainer from '../redux/containers/PropositionsContainer';
import LabelsContainer from '../redux/containers/LabelsContainer';

class ParsingDemo extends Component {
    submitUtterance = () => {
      const { utterance, submitUtterance } = this.props;
      submitUtterance(utterance);
    };

    renderToggles() {
      const {
        showPropBank, showVerbNet, showSemantics, functionalSemantics, showOptions,
        togglePropBank, toggleVerbNet, toggleSemantics, toggleFunctionalSemantics, toggleOptions,
      } = this.props;
      return (
        <Accordion fluid>
          <Accordion.Title active={showOptions} index={0} onClick={toggleOptions}>
            <Icon name="dropdown" />
            {' '}
                    View options
          </Accordion.Title>
          <Accordion.Content active={showOptions}>
            <Form>
              <Form.Group>
                <Form.Field>
                  <Checkbox checked={showPropBank} toggle label="PropBank" onChange={togglePropBank} />
                </Form.Field>
                <Form.Field>
                  <Checkbox checked={showVerbNet} toggle label="VerbNet" onChange={toggleVerbNet} />
                </Form.Field>
                <Form.Field>
                  <Checkbox checked={showSemantics} toggle label="Semantics" onChange={toggleSemantics} />
                </Form.Field>
                <Form.Field>
                  <Checkbox
                    disabled={!showSemantics}
                    checked={functionalSemantics}
                    toggle
                    label="VN Index View"
                    onChange={toggleFunctionalSemantics}
                  />
                </Form.Field>
              </Form.Group>
            </Form>
          </Accordion.Content>
        </Accordion>
      );
    }

    renderResult() {
      const { mobile } = this.props;

      return (
        <Segment textAlign="left">
          <LabelsContainer />
          <PropositionsContainer showTabs={mobile} />
          { this.renderToggles() }
        </Segment>
      );
    }

    render() {
      const {
        mobile, loading, success, failure, errorMessage, setUtterance,
      } = this.props;

      let predictionResult = '';
      if (!success) {
        if (failure && errorMessage) {
          predictionResult = <Message negative content={errorMessage} />;
        }
      } else {
        predictionResult = this.renderResult();
      }

      return (
        <div>
          <Form loading={loading} onSubmit={this.submitUtterance}>
            <Form.Input
              size={mobile ? 'small' : 'huge'}
              placeholder="Enter a sentence, or see a random example..."
              action={(
                <Button
                  disabled={loading}
                  primary
                  size={mobile ? 'small' : 'huge'}
                  onClick={this.submitUtterance}
                  content="Try it out!"
                />
                        )}
              onChange={setUtterance}
            />
          </Form>
          { predictionResult }
        </div>
      );
    }
}

ParsingDemo.propTypes = {
  mobile: PropTypes.bool,

  utterance: PropTypes.string.isRequired,
  errorMessage: PropTypes.string.isRequired,
  loading: PropTypes.bool.isRequired,
  success: PropTypes.bool.isRequired,
  failure: PropTypes.bool.isRequired,
  setUtterance: PropTypes.func.isRequired,
  submitUtterance: PropTypes.func.isRequired,

  showPropBank: PropTypes.bool.isRequired,
  showVerbNet: PropTypes.bool.isRequired,
  showSemantics: PropTypes.bool.isRequired,
  functionalSemantics: PropTypes.bool.isRequired,
  showOptions: PropTypes.bool.isRequired,
  togglePropBank: PropTypes.func.isRequired,
  toggleVerbNet: PropTypes.func.isRequired,
  toggleSemantics: PropTypes.func.isRequired,
  toggleFunctionalSemantics: PropTypes.func.isRequired,
  toggleOptions: PropTypes.func.isRequired,

};

ParsingDemo.defaultProps = {
  mobile: false,
};

export default ParsingDemo;
