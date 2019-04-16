import { Container, Header } from 'semantic-ui-react';
import PropTypes from 'prop-types';
import React from 'react';
import ParsingDemo from './ParsingDemo';

const HomepageHeading = ({ mobile }) => (
  <Container text>
    <Header
      as="h1"
      textAlign="center"
      content="VerbNet Parser"
      inverted
      style={{
        fontSize: mobile ? '2em' : '4em',
        fontWeight: 'normal',
        marginBottom: 0,
        marginTop: mobile ? '1.5em' : '3em',
      }}
    />
    <ParsingDemo mobile={mobile} />
  </Container>
);

HomepageHeading.propTypes = {
  mobile: PropTypes.bool,
};

HomepageHeading.defaultProps = {
  mobile: false,
};

export default HomepageHeading;
