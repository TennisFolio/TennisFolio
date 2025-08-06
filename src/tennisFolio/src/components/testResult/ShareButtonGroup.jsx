import React from 'react';
import { useParams } from 'react-router-dom';
import { useCopyToClipboard } from '@uidotdev/usehooks';

import {
  FacebookShareButton,
  FacebookIcon,
  TwitterShareButton,
  XIcon,
} from 'react-share';
import './shareButtonGroup.css';
import { base_url } from '@/constants';

function ShareButtonGroup() {
  const param = useParams();
  const category = param.category;
  const query = param.query;
  const url = `${base_url}/test/${category}/result/${query}`;
  const [copiedText, copy] = useCopyToClipboard();

  return (
    <div>
      <h3 className="share-button-title">친구에게 공유하기</h3>
      <div className="share-button-group">
        <FacebookShareButton url={url}>
          <FacebookIcon size={48} round />
        </FacebookShareButton>

        <TwitterShareButton url={url}>
          <XIcon round size={48} />
        </TwitterShareButton>
        <button
          onClick={() => {
            copy(`${base_url}/test/${category}/result/${query}`);
            alert('링크가 복사되었습니다!');
          }}
          className="url-copy-button"
        >
          URL
        </button>
      </div>
    </div>
  );
}
export default ShareButtonGroup;
