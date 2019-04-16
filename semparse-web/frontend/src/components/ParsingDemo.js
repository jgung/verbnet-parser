import React, { Component } from 'react';
import _ from 'lodash';
import {
  Accordion,
  Button,
  Form,
  Icon,
  Label,
  Message,
  Segment,
} from 'semantic-ui-react';
import PropTypes from 'prop-types';
import Propositions from './Propositions';

const MAX_LENGTH = 512;


const examples = [
  'If you liked the music we were playing last night, you will absolutely love what we\'re playing tomorrow!',
  'I didn\'t want to spend the next thirty years writing about bad things happening in the same small town',
  'John ate an apple and dropped the core into the trash',
  'Alice sold Bob her laptop for $300',
  'Where did John take the train after leaving the office?',
  'To whom did John lend his guitar last week?',
  'Alice went to the corner shop to buy some eggs, milk and bread for breakfast',
  'Mary discovered the impending layoffs from taking a look at her boss\'s computer',
  'In 2018, crude futures rose 74 cents, or 1.4 percent, to $53.95 from $53.21',
];

function randomExample() {
  return _.sample(examples);
}

class ParsingDemo extends Component {
    state = {
      utterance: '',
      message: '',
      success: false,
      failure: false,
      loading: false,
      errorMessage: '',

      activeIndex: 0,
      showOptions: false,
      showPropBank: false,
      showVerbNet: true,
      showSemantics: true,
    };

    handleTabChange = (e, { activeIndex }) => {
      this.setState({ activeIndex });
    };

    setTabIndex = (activeIndex) => {
      this.setState({ activeIndex });
    };

    togglePropBank = () => {
      this.setState(prevState => ({ showPropBank: !prevState.showPropBank }));
    };

    toggleVerbNet = () => {
      this.setState(prevState => ({ showVerbNet: !prevState.showVerbNet }));
    };

    toggleSemantics = () => {
      this.setState(prevState => ({ showSemantics: !prevState.showSemantics }));
    };

    toggleOptions = () => {
      this.setState(prevState => ({ showOptions: !prevState.showOptions }));
    };

    submitUtterance = () => {
      const { utterance } = this.state;
      const inputUtterance = !utterance ? randomExample() : utterance;

      if (inputUtterance.length > MAX_LENGTH) {
        this.setState({
          failure: true,
          errorMessage: `Sorry, the maximum utterance length is ${MAX_LENGTH} characters. Please try a shorter sentence.`,
          message: '',
          success: false,
        });
      } else {
        this.setState({
          success: false, failure: false, loading: true, activeIndex: 0,
        });
        fetch(`/predict/semantics?utterance=${encodeURIComponent(inputUtterance)}`)
          .then((response) => {
            if (response.ok) {
              return response.json();
            }
            throw new Error('Something went wrong');
          },
          () => {
            this.setState({
              success: false,
              failure: true,
              loading: false,
              message: '',
              errorMessage: "Sorry, we're unable to make predictions at this time.",
            });
          })
          .then((message) => {
            this.setState({
              success: true, failure: false, loading: false, message,
            });
          })
          .catch(() => {
            this.setState({
              success: false,
              failure: true,
              loading: false,
              message: '',
              errorMessage: 'Sorry, something went wrong and we were unable to handle your request.',
            });
          });
      }
    };

    updateUtterance = (e) => {
      const { value } = e.target;
      this.setState(prevState => ({ ...prevState, utterance: value }));
    };

    renderResult() {
      const {
        showPropBank, showVerbNet, showSemantics, showOptions, message, activeIndex,
      } = this.state;

      const { tokens, props } = message;
      if (!props) {
        return '';
      }

      const propIndex = Math.min(activeIndex, props.length - 1);

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
              onClick={() => this.setTabIndex(index)}
            />
          );
        }
        return ` ${text} `;
      });

      return (
        <Segment>
          <Message style={{ flexDirection: 'row' }}>
            {sentence}
          </Message>
          <Propositions
            propIndex={propIndex}
            handleTabChange={this.handleTabChange}
            propositions={props}
            showPropBank={showPropBank}
            showVerbNet={showVerbNet}
            showSemantics={showSemantics}
          />
          <Accordion fluid>
            <Accordion.Title active={showOptions} index={0} onClick={this.toggleOptions}>
              <Icon name="dropdown" />
              {' '}
              View options
            </Accordion.Title>
            <Accordion.Content active={showOptions}>
              <Button.Group>
                <Button primary={showPropBank} content="PropBank" onClick={this.togglePropBank} />
                <Button primary={showVerbNet} content="VerbNet" onClick={this.toggleVerbNet} />
                <Button primary={showSemantics} content="Semantics" onClick={this.toggleSemantics} />
              </Button.Group>
            </Accordion.Content>
          </Accordion>
        </Segment>
      );
    }

    render() {
      const { mobile } = this.props;
      const {
        loading, success, failure, errorMessage,
      } = this.state;

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
                  content={mobile ? 'Try it out!' : 'Try it out!'}
                />
                        )}
              onChange={this.updateUtterance}
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
};

ParsingDemo.defaultProps = {
  mobile: false,
};

export default ParsingDemo;
