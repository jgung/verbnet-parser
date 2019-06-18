import React, { Component } from 'react';
import _ from 'lodash';
import {
  Accordion,
  Button, Checkbox,
  Form,
  Icon,
  Message,
  Segment,
} from 'semantic-ui-react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import Propositions from './Propositions';
import * as actions from '../redux/actions';
import { getDemoState } from '../redux/selectors';
import Labels from './Labels';

const MAX_LENGTH = 512;

let examplesIndex = 0;

const examples = _.shuffle([
  'If you liked the music we were playing last night, you will absolutely love what we\'re playing tomorrow!',
  'I didn\'t want to spend the next thirty years writing about bad things happening in the same small town',
  'John ate an apple and dropped the core into the trash',
  'Alice sold Bob her laptop for $300',
  'Where did John take the train after leaving the office?',
  'To whom did John lend his guitar last week?',
  'Alice went to the corner shop to buy some eggs, milk and bread for breakfast',
  'Mary discovered the impending layoffs from taking a look at her boss\'s computer',
  'In 2018, crude futures rose 74 cents, or 1.4 percent, to $53.95 from $53.21',
]);

function randomExample() {
  const result = examples[examplesIndex % examples.length];
  examplesIndex += 1;
  return result;
}

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
          <Labels />
          <Propositions showTabs={mobile} />
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
          {
                predictionResult
            }
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

const mapStateToProps = state => ({
  ...getDemoState(state),
});

const mapDispatchToProps = dispatch => (
  {
    setUtterance: (e) => {
      const { value } = e.target;
      dispatch(actions.setUtterance(value));
    },
    handleTabChange: (e, { activeIndex }) => {
      dispatch(actions.setTabIndex(activeIndex));
    },
    submitUtterance: (utterance) => {
      const inputUtterance = !utterance ? randomExample() : utterance;

      if (inputUtterance.length > MAX_LENGTH) {
        dispatch(actions.submitUtteranceFailure(
          `Sorry, the maximum utterance length is ${MAX_LENGTH} characters. Please try a shorter sentence.`,
        ));
      } else {
        dispatch(actions.submitUtteranceRequest(utterance));
        fetch(`/predict/semantics?utterance=${encodeURIComponent(inputUtterance)}`)
          .then((response) => {
            if (response.ok) {
              return response.json();
            }
            throw new Error('Something went wrong');
          },
          () => {
            dispatch(actions.submitUtteranceFailure("Sorry, we're unable to make predictions at this time."));
          })
          .then((message) => {
            dispatch(actions.submitUtteranceSuccess(message));
          })
          .catch(() => {
            dispatch(actions.submitUtteranceFailure('Sorry, something went wrong and we were unable to handle your request.'));
          });
      }
    },
    togglePropBank: () => dispatch(actions.togglePropbank()),
    toggleVerbNet: () => dispatch(actions.toggleVerbnet()),
    toggleSemantics: () => dispatch(actions.toggleSemantics()),
    toggleFunctionalSemantics: () => dispatch(actions.toggleIndexView()),
    toggleOptions: () => dispatch(actions.toggleViewOptions()),
  }
);

export default connect(mapStateToProps, mapDispatchToProps)(ParsingDemo);
